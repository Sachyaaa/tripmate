import React, { useState, useEffect } from 'react'
import toast from 'react-hot-toast'
import client from '../../api/client'

export default function SettleUpTab({ tripId, members }) {
  const [balances, setBalances] = useState([])
  const [settlements, setSettlements] = useState([])
  const [loading, setLoading] = useState(true)
  const [markingPaid, setMarkingPaid] = useState({})

  const fetchData = async () => {
    try {
      const [bRes, sRes] = await Promise.all([
        client.get(`/api/trips/${tripId}/balances`),
        client.get(`/api/trips/${tripId}/settlements`),
      ])
      setBalances(bRes.data)
      setSettlements(sRes.data)
    } catch {
      toast.error('Failed to load settlement data')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchData() }, [tripId])

  const handleMarkPaid = async (s) => {
    const key = `${s.fromUserId}-${s.toUserId}`
    setMarkingPaid(p => ({ ...p, [key]: true }))
    try {
      await client.put('/api/settlements/mark-paid', {
        fromUserId: s.fromUserId,
        toUserId: s.toUserId,
        tripId,
      })
      toast.success('Marked as paid!')
      await fetchData()
    } catch {
      toast.error('Failed to mark as paid')
    } finally {
      setMarkingPaid(p => ({ ...p, [key]: false }))
    }
  }

  const formatAmount = (n) => parseFloat(n).toLocaleString('en-IN', { minimumFractionDigits: 2 })

  if (loading) return <div className="spinner" />

  return (
    <div style={{ maxWidth: 680 }}>
      {/* Balance cards */}
      <h2 style={{ fontWeight: 700, marginBottom: 16 }}>Member Balances</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 14, marginBottom: 32 }}>
        {balances.map(b => {
          const net = parseFloat(b.netBalance)
          const positive = net >= 0
          return (
            <div key={b.userId} className="card card-sm"
              style={{ borderLeft: `4px solid ${positive ? 'var(--success)' : 'var(--danger)'}` }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
                <div className="avatar" style={{ background: b.color || '#888' }}>
                  {(b.displayName || '?')[0].toUpperCase()}
                </div>
                <span style={{ fontWeight: 600, fontSize: 14 }}>{b.displayName}</span>
              </div>
              <p style={{ fontSize: 20, fontWeight: 800, color: positive ? 'var(--success)' : 'var(--danger)' }}>
                {positive ? '+' : ''}₹{formatAmount(Math.abs(net))}
              </p>
              <p style={{ fontSize: 12, color: 'var(--text-light)', marginTop: 2 }}>
                {positive ? 'is owed money' : 'owes money'}
              </p>
            </div>
          )
        })}
      </div>

      {/* Settlement transactions */}
      <h2 style={{ fontWeight: 700, marginBottom: 16 }}>Suggested Payments</h2>
      {settlements.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 40 }}>
          <div style={{ fontSize: 40, marginBottom: 12 }}>🎉</div>
          <p style={{ fontSize: 18, fontWeight: 700, color: 'var(--success)', marginBottom: 4 }}>All Settled!</p>
          <p className="text-muted">Everyone is even — no payments needed.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {settlements.map((s, i) => {
            const key = `${s.fromUserId}-${s.toUserId}`
            return (
              <div key={i} className="card card-sm"
                style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div className="avatar" style={{ background: s.fromUserColor || '#888' }}>
                  {(s.fromUserName || '?')[0].toUpperCase()}
                </div>
                <div style={{ flex: 1 }}>
                  <p style={{ fontWeight: 600, fontSize: 14 }}>
                    <strong>{s.fromUserName}</strong>
                    <span style={{ color: 'var(--text-light)', margin: '0 8px' }}>pays</span>
                    <strong>{s.toUserName}</strong>
                  </p>
                  <p style={{ fontSize: 18, fontWeight: 800, color: 'var(--primary)', marginTop: 2 }}>
                    ₹{formatAmount(s.amount)}
                  </p>
                </div>
                <div className="avatar" style={{ background: s.toUserColor || '#888' }}>
                  {(s.toUserName || '?')[0].toUpperCase()}
                </div>
                <button className="btn btn-secondary btn-sm" onClick={() => handleMarkPaid(s)}
                  disabled={markingPaid[key]}>
                  {markingPaid[key] ? '…' : '✓ Mark Paid'}
                </button>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
