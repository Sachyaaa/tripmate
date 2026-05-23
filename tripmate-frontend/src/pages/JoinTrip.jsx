import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import client from '../api/client'
import { useAuth } from '../contexts/AuthContext'

export default function JoinTrip() {
  const { token } = useParams()
  const { token: authToken } = useAuth()
  const navigate = useNavigate()
  const [preview, setPreview] = useState(null)
  const [loading, setLoading] = useState(true)
  const [joining, setJoining] = useState(false)

  useEffect(() => {
    client.get(`/api/trips/join/${token}`)
      .then(r => setPreview(r.data))
      .catch(() => toast.error('Invalid or expired invite link'))
      .finally(() => setLoading(false))
  }, [token])

  const handleJoin = async () => {
    if (!authToken) {
      navigate(`/login?redirect=/join/${token}`)
      return
    }
    setJoining(true)
    try {
      const { data } = await client.post(`/api/trips/join/${token}`)
      toast.success(`Joined "${data.name}"!`)
      navigate(`/trips/${data.id}`)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to join trip')
      setJoining(false)
    }
  }

  if (loading) return (
    <div className="auth-page"><div className="spinner" /></div>
  )

  if (!preview) return (
    <div className="auth-page">
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <div style={{ fontSize: 48, marginBottom: 16 }}>🔗</div>
        <h2 style={{ marginBottom: 8 }}>Invalid invite link</h2>
        <p className="text-muted" style={{ marginBottom: 20 }}>This link may have expired or be incorrect.</p>
        <button className="btn btn-primary" onClick={() => navigate('/')}>Go Home</button>
      </div>
    </div>
  )

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <div style={{ fontSize: 56, marginBottom: 12 }}>{preview.coverEmoji || '✈️'}</div>
        <h2 style={{ fontSize: 24, fontWeight: 700, marginBottom: 6 }}>{preview.name}</h2>
        <p className="text-muted" style={{ marginBottom: 24 }}>
          {preview.memberCount} member{preview.memberCount !== 1 ? 's' : ''} · You're invited!
        </p>
        {authToken ? (
          <button className="btn btn-primary btn-lg btn-block" onClick={handleJoin} disabled={joining}>
            {joining ? 'Joining…' : `Join "${preview.name}"`}
          </button>
        ) : (
          <>
            <p className="text-muted" style={{ marginBottom: 16 }}>Sign in to join this trip</p>
            <button className="btn btn-primary btn-block" onClick={() => navigate(`/login?redirect=/join/${token}`)}>
              Sign In to Join
            </button>
            <button className="btn btn-ghost btn-block" style={{ marginTop: 8 }}
              onClick={() => navigate(`/register`)}>
              Create Account
            </button>
          </>
        )}
      </div>
    </div>
  )
}
