import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function Landing() {
  const navigate = useNavigate()
  const { token } = useAuth()

  return (
    <div style={{ minHeight: '100vh', background: 'linear-gradient(135deg, #eef2ff 0%, #f0fdf4 100%)' }}>
      {/* Nav */}
      <nav className="navbar">
        <span className="nav-brand">✈️ TripMate</span>
        <div className="nav-actions">
          {token ? (
            <button className="btn btn-primary" onClick={() => navigate('/trips')}>My Trips</button>
          ) : (
            <>
              <button className="btn btn-ghost" onClick={() => navigate('/login')}>Log in</button>
              <button className="btn btn-primary" onClick={() => navigate('/register')}>Sign up</button>
            </>
          )}
        </div>
      </nav>

      {/* Hero */}
      <div style={{ maxWidth: 720, margin: '0 auto', padding: '100px 24px 60px', textAlign: 'center' }}>
        <div style={{ fontSize: 64, marginBottom: 16 }}>✈️</div>
        <h1 style={{ fontSize: 52, fontWeight: 800, color: '#1e293b', lineHeight: 1.15, marginBottom: 16 }}>
          Plan group trips,<br />
          <span style={{ color: 'var(--primary)' }}>together.</span>
        </h1>
        <p style={{ fontSize: 18, color: 'var(--text-light)', maxWidth: 480, margin: '0 auto 40px', lineHeight: 1.7 }}>
          Build itineraries, track expenses and split costs with everyone — all in one place.
        </p>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'center', flexWrap: 'wrap' }}>
          <button className="btn btn-primary btn-lg" onClick={() => navigate('/register')}>
            Create a Trip
          </button>
          <button className="btn btn-ghost btn-lg" onClick={() => navigate('/login')}>
            Join with Invite Link
          </button>
        </div>
      </div>

      {/* Features */}
      <div style={{ maxWidth: 900, margin: '0 auto', padding: '0 24px 80px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: 20 }}>
        {[
          { emoji: '🗓️', title: 'Shared Itinerary', desc: 'Build a day-by-day plan together with drag-and-drop reordering.' },
          { emoji: '💸', title: 'Expense Splitting', desc: 'Track who paid what and split costs equally or with custom amounts.' },
          { emoji: '⚖️', title: 'Settle Up', desc: 'Minimal transactions calculated automatically so no one overpays.' },
          { emoji: '🔗', title: 'Invite by Link', desc: 'Share a 12-character invite code — anyone can join in one click.' },
        ].map(f => (
          <div key={f.title} className="card" style={{ textAlign: 'center' }}>
            <div style={{ fontSize: 36, marginBottom: 12 }}>{f.emoji}</div>
            <h3 style={{ fontWeight: 700, marginBottom: 8 }}>{f.title}</h3>
            <p style={{ color: 'var(--text-light)', fontSize: 14 }}>{f.desc}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
