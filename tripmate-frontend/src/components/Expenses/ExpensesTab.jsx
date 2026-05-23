import React, { useState, useEffect } from 'react'
import toast from 'react-hot-toast'
import client from '../../api/client'
import { useAuth } from '../../contexts/AuthContext'

const CATEGORY_ICONS = { hotel: '🏨', food: '🍽️', transport: '🚗', activity: '🎯', other: '📌' }

function AddExpenseModal({ tripId, members, onClose, onAdded }) {
  const { user } = useAuth()
  const [form, setForm] = useState({
    title: '', amount: '', currency: 'INR', paidByUserId: user?.id || '',
    category: 'other', splitType: 'EQUAL', date: new Date().toISOString().split('T')[0],
  })
  const [customSplits, setCustomSplits] = useState({})
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (form.splitType === 'EQUAL') setCustomSplits({})
  }, [form.splitType])

  const splitTotal = Object.values(customSplits).reduce((s, v) => s + (parseFloat(v) || 0), 0)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.amount || parseFloat(form.amount) <= 0) { toast.error('Enter a valid amount'); return }
    if (form.splitType === 'CUSTOM') {
      const total = parseFloat(form.amount)
      if (Math.abs(splitTotal - total) > 0.01) { toast.error(`Split amounts must sum to ${total}`); return }
    }
    setLoading(true)
    try {
      const payload = {
        ...form,
        amount: parseFloat(form.amount),
        splits: form.splitType === 'CUSTOM'
          ? Object.entries(customSplits).map(([userId, amount]) => ({ userId, amount: parseFloat(amount) }))
          : null
      }
      const { data } = await client.post(`/api/trips/${tripId}/expenses`, payload)
      onAdded(data)
      toast.success('Expense added')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add expense')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h3 className="modal-title">Add Expense</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Title *</label>
            <input className="form-input" placeholder="e.g. Hotel booking" value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))} required />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">Amount *</label>
              <input type="number" step="0.01" min="0.01" className="form-input" placeholder="0.00"
                value={form.amount} onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} required />
            </div>
            <div className="form-group">
              <label className="form-label">Category</label>
              <select className="form-input" value={form.category} onChange={e => setForm(f => ({ ...f, category: e.target.value }))}>
                {Object.entries(CATEGORY_ICONS).map(([k, v]) => <option key={k} value={k}>{v} {k}</option>)}
              </select>
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">Paid By *</label>
              <select className="form-input" value={form.paidByUserId} onChange={e => setForm(f => ({ ...f, paidByUserId: e.target.value }))} required>
                {members.map(m => <option key={m.userId} value={m.userId}>{m.displayName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Date</label>
              <input type="date" className="form-input" value={form.date}
                onChange={e => setForm(f => ({ ...f, date: e.target.value }))} />
            </div>
          </div>

          {/* Split type toggle */}
          <div className="form-group">
            <label className="form-label">Split</label>
            <div style={{ display: 'flex', gap: 8 }}>
              {['EQUAL', 'CUSTOM'].map(t => (
                <button key={t} type="button"
                  className={`btn btn-sm ${form.splitType === t ? 'btn-primary' : 'btn-ghost'}`}
                  onClick={() => setForm(f => ({ ...f, splitType: t }))}>
                  {t === 'EQUAL' ? '⚖️ Equal' : '✏️ Custom'}
                </button>
              ))}
            </div>
          </div>

          {form.splitType === 'CUSTOM' && (
            <div style={{ background: 'var(--bg)', borderRadius: 8, padding: 12, marginBottom: 16 }}>
              <p style={{ fontSize: 13, fontWeight: 600, marginBottom: 10 }}>Custom split amounts</p>
              {members.map(m => (
                <div key={m.userId} style={{ display: 'grid', gridTemplateColumns: '1fr 120px', gap: 8, marginBottom: 8, alignItems: 'center' }}>
                  <span style={{ fontSize: 13 }}>{m.displayName}</span>
                  <input type="number" step="0.01" min="0" className="form-input"
                    placeholder="0.00" value={customSplits[m.userId] || ''}
                    onChange={e => setCustomSplits(s => ({ ...s, [m.userId]: e.target.value }))}
                    style={{ fontSize: 13 }} />
                </div>
              ))}
              <p style={{ fontSize: 12, color: Math.abs(splitTotal - (parseFloat(form.amount) || 0)) < 0.01 ? 'var(--success)' : 'var(--danger)', marginTop: 4 }}>
                Total: ₹{splitTotal.toFixed(2)} / ₹{parseFloat(form.amount || 0).toFixed(2)}
              </p>
            </div>
          )}

          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Adding…' : 'Add Expense'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

function ExpenseCard({ expense, onDelete }) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div className="card card-sm" style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer' }}
        onClick={() => setExpanded(e => !e)}>
        <span style={{ fontSize: 24 }}>{CATEGORY_ICONS[expense.category?.toLowerCase()] || '📌'}</span>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <p style={{ fontWeight: 600 }}>{expense.title}</p>
            <p style={{ fontWeight: 700, color: 'var(--primary)', flexShrink: 0, marginLeft: 8 }}>
              ₹{parseFloat(expense.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 2 }}>
            <span style={{ fontSize: 12, color: 'var(--text-light)' }}>Paid by <strong>{expense.paidBy?.displayName}</strong></span>
            {expense.date && <span style={{ fontSize: 12, color: 'var(--text-light)' }}>· {expense.date}</span>}
            <span className="badge badge-primary" style={{ fontSize: 11 }}>{expense.splitType}</span>
          </div>
        </div>
        <span style={{ color: 'var(--text-light)', fontSize: 12 }}>{expanded ? '▲' : '▼'}</span>
      </div>

      {expanded && (
        <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid var(--border)' }}>
          <p style={{ fontSize: 12, fontWeight: 600, marginBottom: 8, color: 'var(--text-light)' }}>Split breakdown</p>
          {expense.splits?.map(s => (
            <div key={s.userId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
              <span>{s.displayName}</span>
              <span style={{ color: s.paid ? 'var(--success)' : 'var(--text)' }}>
                ₹{parseFloat(s.amount).toFixed(2)} {s.paid ? '✓' : ''}
              </span>
            </div>
          ))}
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 10 }}>
            <button className="btn btn-danger btn-sm" onClick={() => onDelete(expense.id)}>Delete</button>
          </div>
        </div>
      )}
    </div>
  )
}

export default function ExpensesTab({ tripId, members }) {
  const [expenses, setExpenses] = useState([])
  const [loading, setLoading] = useState(true)
  const [showAdd, setShowAdd] = useState(false)

  useEffect(() => {
    client.get(`/api/trips/${tripId}/expenses`)
      .then(r => setExpenses(r.data))
      .catch(() => toast.error('Failed to load expenses'))
      .finally(() => setLoading(false))
  }, [tripId])

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this expense?')) return
    try {
      await client.delete(`/api/expenses/${id}`)
      setExpenses(e => e.filter(x => x.id !== id))
      toast.success('Expense deleted')
    } catch {
      toast.error('Failed to delete expense')
    }
  }

  const total = expenses.reduce((s, e) => s + parseFloat(e.amount), 0)

  if (loading) return <div className="spinner" />

  return (
    <div>
      {expenses.length > 0 && (
        <div className="card" style={{ marginBottom: 20, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <p className="text-muted" style={{ fontSize: 13 }}>Total Trip Spend</p>
            <p style={{ fontSize: 28, fontWeight: 800, color: 'var(--primary)' }}>
              ₹{total.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <button className="btn btn-primary" onClick={() => setShowAdd(true)}>+ Add Expense</button>
        </div>
      )}

      {expenses.length === 0 ? (
        <div className="empty-state">
          <span className="emoji">💸</span>
          <p style={{ fontWeight: 600, marginBottom: 8 }}>No expenses yet</p>
          <p style={{ marginBottom: 20 }}>Track what everyone paid</p>
          <button className="btn btn-primary" onClick={() => setShowAdd(true)}>Add First Expense</button>
        </div>
      ) : (
        <>
          {expenses.map(e => <ExpenseCard key={e.id} expense={e} onDelete={handleDelete} />)}
          <button className="fab" onClick={() => setShowAdd(true)} title="Add expense">+</button>
        </>
      )}

      {showAdd && (
        <AddExpenseModal tripId={tripId} members={members} onClose={() => setShowAdd(false)}
          onAdded={(e) => { setExpenses(prev => [e, ...prev]); setShowAdd(false) }} />
      )}
    </div>
  )
}
