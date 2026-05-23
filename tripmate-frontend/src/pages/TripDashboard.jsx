import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import client from '../api/client'
import { useAuth } from '../contexts/AuthContext'
import ItineraryTab from '../components/Itinerary/ItineraryTab'
import ExpensesTab from '../components/Expenses/ExpensesTab'
import SettleUpTab from '../components/SettleUp/SettleUpTab'
import MembersTab from '../components/Members/MembersTab'

const TABS = [
  { id: 'itinerary', label: '🗓️ Itinerary' },
  { id: 'expenses', label: '💸 Expenses' },
  { id: 'settle', label: '⚖️ Settle Up' },
  { id: 'members', label: '👥 Members' },
]

export default function TripDashboard() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [trip, setTrip] = useState(null)
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('itinerary')

  const fetchTrip = () => {
    client.get(`/api/trips/${id}`)
      .then(r => setTrip(r.data))
      .catch(err => {
        if (err.response?.status === 403) {
          toast.error('You are not a member of this trip')
          navigate('/trips')
        } else {
          toast.error('Failed to load trip')
        }
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchTrip() }, [id])

  if (loading) return (
    <div style={{ minHeight: '100vh' }}>
      <nav className="navbar"><span className="nav-brand">✈️ TripMate</span></nav>
      <div className="spinner" />
    </div>
  )

  if (!trip) return null

  return (
    <div style={{ minHeight: '100vh' }}>
      {/* Navbar */}
      <nav className="navbar">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <button className="btn btn-ghost btn-sm" onClick={() => navigate('/trips')}>← Trips</button>
          <span style={{ color: 'var(--border)' }}>|</span>
          <span className="nav-brand" style={{ fontSize: 16 }}>
            {trip.coverEmoji} {trip.name}
          </span>
        </div>
        <div className="nav-actions">
          <span style={{ fontSize: 14, color: 'var(--text-light)' }}>{user?.displayName}</span>
          <button className="btn btn-ghost btn-sm" onClick={() => { logout(); navigate('/') }}>Logout</button>
        </div>
      </nav>

      {/* Trip header */}
      <div style={{ background: 'linear-gradient(135deg, var(--primary) 0%, var(--secondary) 100%)', color: '#fff', padding: '28px 24px 0' }}>
        <div style={{ maxWidth: 1100, margin: '0 auto' }}>
          <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 20 }}>
            <div>
              <h1 style={{ fontSize: 28, fontWeight: 800, marginBottom: 4 }}>
                {trip.coverEmoji} {trip.name}
              </h1>
              {(trip.startDate || trip.endDate) && (
                <p style={{ opacity: .85, fontSize: 14 }}>
                  📅 {trip.startDate || '?'} → {trip.endDate || '?'}
                </p>
              )}
              {trip.description && <p style={{ opacity: .75, fontSize: 14, marginTop: 4 }}>{trip.description}</p>}
            </div>
            <div style={{ display: 'flex', gap: 4, marginTop: 4 }}>
              {trip.members?.slice(0, 5).map(m => (
                <div key={m.id} className="avatar" title={m.displayName}
                  style={{ background: m.color, border: '2px solid rgba(255,255,255,.6)' }}>
                  {(m.displayName || '?')[0].toUpperCase()}
                </div>
              ))}
              {trip.members?.length > 5 && (
                <div className="avatar" style={{ background: 'rgba(255,255,255,.25)', fontSize: 11 }}>
                  +{trip.members.length - 5}
                </div>
              )}
            </div>
          </div>

          {/* Tab bar */}
          <div style={{ display: 'flex', gap: 4 }}>
            {TABS.map(t => (
              <button key={t.id} onClick={() => setActiveTab(t.id)}
                style={{
                  padding: '10px 18px', border: 'none', cursor: 'pointer', fontSize: 14, fontWeight: 600,
                  background: activeTab === t.id ? '#fff' : 'rgba(255,255,255,.15)',
                  color: activeTab === t.id ? 'var(--primary)' : '#fff',
                  borderRadius: '8px 8px 0 0', transition: 'all .15s'
                }}>
                {t.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Tab content */}
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '28px 24px' }}>
        {activeTab === 'itinerary' && <ItineraryTab tripId={id} initialDays={trip.days} members={trip.members} />}
        {activeTab === 'expenses' && <ExpensesTab tripId={id} members={trip.members} />}
        {activeTab === 'settle' && <SettleUpTab tripId={id} members={trip.members} />}
        {activeTab === 'members' && <MembersTab tripId={id} trip={trip} onTripUpdated={fetchTrip} />}
      </div>
    </div>
  )
}
