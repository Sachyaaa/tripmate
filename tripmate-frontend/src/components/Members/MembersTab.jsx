import React, { useState } from 'react'
import toast from 'react-hot-toast'
import client from '../../api/client'
import { useAuth } from '../../contexts/AuthContext'

export default function MembersTab({ tripId, trip, onTripUpdated }) {
  const { user } = useAuth()
  const [inviteUrl, setInviteUrl] = useState('')
  const [loadingInvite, setLoadingInvite] = useState(false)
  const [downloading, setDownloading] = useState(false)

  const fetchInviteUrl = async () => {
    if (inviteUrl) return
    setLoadingInvite(true)
    try {
      const { data } = await client.get(`/api/trips/${tripId}/invite`)
      setInviteUrl(data.inviteUrl)
    } catch {
      toast.error('Failed to get invite link')
    } finally {
      setLoadingInvite(false)
    }
  }

  const copyInvite = async () => {
    try {
      await navigator.clipboard.writeText(inviteUrl)
      toast.success('Invite link copied!')
    } catch {
      toast.error('Could not copy — please copy manually')
    }
  }

  const downloadPdf = async () => {
    setDownloading(true)
    try {
      const response = await client.get(`/api/trips/${tripId}/export/pdf`, { responseType: 'blob' })
      const url = URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
      const a = document.createElement('a')
      a.href = url
      a.download = `${trip.name.replace(/\s+/g, '-')}-summary.pdf`
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)
      toast.success('PDF downloaded')
    } catch {
      toast.error('Failed to generate PDF')
    } finally {
      setDownloading(false)
    }
  }

  const members = trip?.members || []

  return (
    <div style={{ maxWidth: 680 }}>
      {/* Member list */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ fontWeight: 700 }}>Members ({members.length})</h2>
        <button className="btn btn-secondary btn-sm" onClick={downloadPdf} disabled={downloading}>
          {downloading ? '⏳ Generating…' : '📄 Download PDF Summary'}
        </button>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginBottom: 32 }}>
        {members.map(m => (
          <div key={m.id} className="card card-sm" style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
            <div className="avatar avatar-lg" style={{ background: m.color }}>
              {(m.displayName || '?')[0].toUpperCase()}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <p style={{ fontWeight: 600 }}>{m.displayName}</p>
                {m.userId === user?.id && (
                  <span className="badge badge-primary" style={{ fontSize: 11 }}>You</span>
                )}
              </div>
              <p style={{ fontSize: 13, color: 'var(--text-light)' }}>{m.email}</p>
            </div>
            <span className={`badge ${m.role === 'CREATOR' ? 'badge-warning' : 'badge-success'}`}>
              {m.role === 'CREATOR' ? '👑 Creator' : '✈️ Member'}
            </span>
          </div>
        ))}
      </div>

      {/* Invite section */}
      <div className="card">
        <h3 style={{ fontWeight: 700, marginBottom: 6 }}>Invite People</h3>
        <p className="text-muted" style={{ marginBottom: 16, fontSize: 14 }}>
          Share the invite link — anyone with it can join this trip.
        </p>

        {!inviteUrl ? (
          <button className="btn btn-secondary" onClick={fetchInviteUrl} disabled={loadingInvite}>
            {loadingInvite ? 'Getting link…' : '🔗 Get Invite Link'}
          </button>
        ) : (
          <>
            <div style={{ display: 'flex', gap: 8, alignItems: 'stretch', marginBottom: 12 }}>
              <input className="form-input" value={inviteUrl} readOnly
                style={{ fontFamily: 'monospace', fontSize: 13, flex: 1 }}
                onClick={e => e.target.select()} />
              <button className="btn btn-primary" onClick={copyInvite}>Copy</button>
            </div>
            <div style={{ background: 'var(--bg)', borderRadius: 8, padding: 12 }}>
              <p style={{ fontSize: 13, color: 'var(--text-light)' }}>
                💡 Tip: You can also share the invite token <strong>{trip?.inviteToken}</strong> directly. Recipients can enter it at <em>tripmate.app/join/TOKEN</em>.
              </p>
              <p style={{ fontSize: 12, color: 'var(--text-light)', marginTop: 6 }}>
                📱 QR Code: Use any QR generator with the invite link above.
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
