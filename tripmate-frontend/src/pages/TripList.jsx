import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import client from '../api/client'
import { useAuth } from '../contexts/AuthContext'

function CreateTripModal({ onClose, onCreated }) {
  const [form, setForm] = useState({ name: '', description: '', startDate: '', endDate: '', coverEmoji: '✈️' })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await client.post('/api/trips', {
        ...form,
        startDate: form.startDate || null,
        endDate: form.endDate || null,
      })
      toast.success('Trip created!')
      onCreated(data)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create trip')
      setLoading(false)
    }
  }

  const EMOJIS = ['✈️', '🏖️', '🏔️', '🌍', '🗺️', '🚂', '🏕️', '🎭', '🍜', '🎿']

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h3 className="modal-title">Create New Trip</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Cover Emoji</label>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {EMOJIS.map(e => (
                <button key={e} type="button"
                  style={{ fontSize: 24, padding: '4px 8px', borderRadius: 8, border: '2px solid',
                    borderColor: form.coverEmoji === e ? 'var(--primary)' : 'var(--border)',
                    background: form.coverEmoji === e ? 'var(--primary-light)' : '#fff', cursor: 'pointer' }}
                  onClick={() => setForm(f => ({ ...f, coverEmoji: e }))}>{e}</button>
              ))}
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Trip Name *</label>
            <input className="form-input" placeholder="e.g. Goa 2025" value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
          </div>
          <div className="form-group">
            <label className="form-label">Description</label>
            <input className="form-input" placeholder="A short description" value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">Start Date</label>
              <input type="date" className="form-input" value={form.startDate}
                onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">End Date</label>
              <input type="date" className="form-input" value={form.endDate}
                onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))} />
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating…' : 'Create Trip'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function TripList() {
  const [trips, setTrips] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    client.get('/api/trips')
      .then(r => setTrips(r.data))
      .catch(() => toast.error('Failed to load trips'))
      .finally(() => setLoading(false))
  }, [])

  const handleCreated = (trip) => {
    setTrips(t => [trip, ...t])
    setShowCreate(false)
    navigate(`/trips/${trip.id}`)
  }

  const initials = (name = '') => name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)

  return (
    <div style={{ minHeight: '100vh' }}>
      <nav className="navbar">
        <span className="nav-brand">✈️ TripMate</span>
        <div className="nav-actions">
          <span style={{ fontSize: 14, color: 'var(--text-light)' }}>Hi, {user?.displayName}</span>
          <button className="btn btn-ghost btn-sm" onClick={() => { logout(); navigate('/') }}>Logout</button>
        </div>
      </nav>

      <div className="page">
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28 }}>
          <div>
            <h1 className="page-title">My Trips</h1>
            <p className="text-muted">All your adventures in one place</p>
          </div>
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>+ New Trip</button>
        </div>

        {loading ? <div className="spinner" /> : trips.length === 0 ? (
          <div className="empty-state">
            <span className="emoji">🌍</span>
            <p style={{ fontSize: 18, fontWeight: 600, marginBottom: 8 }}>No trips yet</p>
            <p style={{ marginBottom: 20 }}>Create your first group trip to get started</p>
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>Create a Trip</button>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 20 }}>
            {trips.map(trip => (
              <div key={trip.id} className="card" style={{ cursor: 'pointer', transition: 'box-shadow .15s' }}
                onClick={() => navigate(`/trips/${trip.id}`)}
                onMouseEnter={e => e.currentTarget.style.boxShadow = 'var(--shadow-lg)'}
                onMouseLeave={e => e.currentTarget.style.boxShadow = 'var(--shadow)'}>
                <div style={{ fontSize: 40, marginBottom: 12 }}>{trip.coverEmoji || '✈️'}</div>
                <h3 style={{ fontWeight: 700, marginBottom: 4 }}>{trip.name}</h3>
                {trip.description && <p className="text-muted" style={{ marginBottom: 10, fontSize: 13 }}>{trip.description}</p>}
                {(trip.startDate || trip.endDate) && (
                  <p style={{ fontSize: 13, color: 'var(--primary)', marginBottom: 10, fontWeight: 500 }}>
                    📅 {trip.startDate || '?'} → {trip.endDate || '?'}
                  </p>
                )}
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 'auto' }}>
                  <div className="avatar" style={{ background: 'var(--primary)', width: 24, height: 24, fontSize: 10 }}>
                    {initials(trip.createdBy?.displayName)}
                  </div>
                  <span className="text-muted" style={{ fontSize: 12 }}>{trip.memberCount} member{trip.memberCount !== 1 ? 's' : ''}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showCreate && <CreateTripModal onClose={() => setShowCreate(false)} onCreated={handleCreated} />}
    </div>
  )
}
