import React, { useState, useEffect } from 'react'
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd'
import toast from 'react-hot-toast'
import client from '../../api/client'

const CATEGORY_ICONS = { HOTEL: '🏨', FOOD: '🍽️', TRANSPORT: '🚗', ACTIVITY: '🎯', OTHER: '📌' }
const CATEGORIES = ['HOTEL', 'FOOD', 'TRANSPORT', 'ACTIVITY', 'OTHER']

function AddItemForm({ dayId, onAdded, onCancel }) {
  const [form, setForm] = useState({ title: '', time: '', category: 'ACTIVITY', notes: '' })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await client.post(`/api/days/${dayId}/items`, form)
      onAdded(data)
    } catch {
      toast.error('Failed to add activity')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ background: 'var(--primary-light)', borderRadius: 8, padding: 12, marginTop: 8 }}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 90px', gap: 8, marginBottom: 8 }}>
        <input className="form-input" placeholder="Activity title" value={form.title}
          onChange={e => setForm(f => ({ ...f, title: e.target.value }))} required style={{ fontSize: 13 }} />
        <input className="form-input" placeholder="10:00" value={form.time}
          onChange={e => setForm(f => ({ ...f, time: e.target.value }))} style={{ fontSize: 13 }} />
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 8 }}>
        <select className="form-input" value={form.category}
          onChange={e => setForm(f => ({ ...f, category: e.target.value }))} style={{ fontSize: 13 }}>
          {CATEGORIES.map(c => <option key={c} value={c}>{CATEGORY_ICONS[c]} {c}</option>)}
        </select>
        <input className="form-input" placeholder="Notes (optional)" value={form.notes}
          onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} style={{ fontSize: 13 }} />
      </div>
      <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
        <button type="button" className="btn btn-ghost btn-sm" onClick={onCancel}>Cancel</button>
        <button type="submit" className="btn btn-primary btn-sm" disabled={loading}>
          {loading ? 'Adding…' : 'Add'}
        </button>
      </div>
    </form>
  )
}

function DayColumn({ day, onItemAdded, onItemDeleted }) {
  const [showAdd, setShowAdd] = useState(false)

  const handleDelete = async (itemId) => {
    try {
      await client.delete(`/api/items/${itemId}`)
      onItemDeleted(day.id, itemId)
    } catch {
      toast.error('Failed to delete item')
    }
  }

  return (
    <div style={{ background: 'var(--card)', borderRadius: 'var(--radius)', boxShadow: 'var(--shadow)', minWidth: 260, maxWidth: 300, padding: 16, flexShrink: 0 }}>
      <div style={{ marginBottom: 12 }}>
        <h3 style={{ fontWeight: 700, fontSize: 15 }}>Day {day.dayNumber}</h3>
        {day.title && <p style={{ fontSize: 12, color: 'var(--text-light)' }}>{day.title}</p>}
        {day.dayDate && <p style={{ fontSize: 12, color: 'var(--text-light)' }}>📅 {day.dayDate}</p>}
      </div>

      <Droppable droppableId={day.id}>
        {(provided, snapshot) => (
          <div ref={provided.innerRef} {...provided.droppableProps}
            style={{ minHeight: 60, background: snapshot.isDraggingOver ? 'var(--primary-light)' : 'transparent', borderRadius: 8, transition: 'background .15s' }}>
            {(day.items || []).map((item, idx) => (
              <Draggable key={item.id} draggableId={item.id} index={idx}>
                {(prov, snap) => (
                  <div ref={prov.innerRef} {...prov.draggableProps} {...prov.dragHandleProps}
                    style={{
                      background: snap.isDragging ? '#fff' : '#f8fafc',
                      borderRadius: 8, padding: '10px 12px', marginBottom: 8,
                      border: '1px solid var(--border)', boxShadow: snap.isDragging ? 'var(--shadow-md)' : 'none',
                      ...prov.draggableProps.style
                    }}>
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 8 }}>
                      <span style={{ fontSize: 16 }}>{CATEGORY_ICONS[item.category] || '📌'}</span>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                          <p style={{ fontWeight: 600, fontSize: 13, wordBreak: 'break-word' }}>{item.title}</p>
                          <button onClick={() => handleDelete(item.id)}
                            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#94a3b8', fontSize: 14, flexShrink: 0, marginLeft: 4 }}>✕</button>
                        </div>
                        {item.time && <p style={{ fontSize: 11, color: 'var(--primary)', fontWeight: 500 }}>⏰ {item.time}</p>}
                        {item.notes && <p style={{ fontSize: 11, color: 'var(--text-light)', marginTop: 2 }}>{item.notes}</p>}
                      </div>
                    </div>
                  </div>
                )}
              </Draggable>
            ))}
            {provided.placeholder}
          </div>
        )}
      </Droppable>

      {showAdd
        ? <AddItemForm dayId={day.id} onCancel={() => setShowAdd(false)}
            onAdded={(item) => { onItemAdded(day.id, item); setShowAdd(false) }} />
        : <button className="btn btn-secondary btn-sm" style={{ width: '100%', marginTop: 8 }}
            onClick={() => setShowAdd(true)}>+ Add Activity</button>}
    </div>
  )
}

export default function ItineraryTab({ tripId, initialDays, members }) {
  const [days, setDays] = useState(initialDays || [])
  const [showAddDay, setShowAddDay] = useState(false)
  const [newDay, setNewDay] = useState({ title: '', dayDate: '' })
  const [addingDay, setAddingDay] = useState(false)

  useEffect(() => { setDays(initialDays || []) }, [initialDays])

  const onDragEnd = async (result) => {
    const { source, destination, draggableId } = result
    if (!destination || (source.droppableId === destination.droppableId && source.index === destination.index)) return

    const dayId = source.droppableId
    if (source.droppableId !== destination.droppableId) return // cross-day reorder not supported

    const dayIdx = days.findIndex(d => d.id === dayId)
    if (dayIdx === -1) return

    const items = [...days[dayIdx].items]
    const [moved] = items.splice(source.index, 1)
    items.splice(destination.index, 0, moved)
    const reordered = items.map((item, i) => ({ ...item, position: i + 1 }))

    setDays(prev => prev.map(d => d.id === dayId ? { ...d, items: reordered } : d))

    try {
      await client.put(`/api/days/${dayId}/reorder`, reordered.map(item => ({ id: item.id, position: item.position })))
    } catch {
      toast.error('Failed to save order')
    }
  }

  const handleAddDay = async (e) => {
    e.preventDefault()
    setAddingDay(true)
    try {
      const { data } = await client.post(`/api/trips/${tripId}/days`, {
        dayNumber: days.length + 1,
        title: newDay.title || null,
        dayDate: newDay.dayDate || null,
      })
      setDays(prev => [...prev, { ...data, items: [] }])
      setNewDay({ title: '', dayDate: '' })
      setShowAddDay(false)
      toast.success('Day added')
    } catch {
      toast.error('Failed to add day')
    } finally {
      setAddingDay(false)
    }
  }

  const handleItemAdded = (dayId, item) => {
    setDays(prev => prev.map(d => d.id === dayId ? { ...d, items: [...(d.items || []), item] } : d))
  }

  const handleItemDeleted = (dayId, itemId) => {
    setDays(prev => prev.map(d => d.id === dayId ? { ...d, items: d.items.filter(i => i.id !== itemId) } : d))
    toast.success('Activity removed')
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 20, overflowX: 'auto', paddingBottom: 16, alignItems: 'flex-start' }}>
        <DragDropContext onDragEnd={onDragEnd}>
          {days.map(day => (
            <DayColumn key={day.id} day={day} onItemAdded={handleItemAdded} onItemDeleted={handleItemDeleted} />
          ))}
        </DragDropContext>

        {/* Add Day button/form */}
        <div style={{ minWidth: 220, flexShrink: 0 }}>
          {showAddDay ? (
            <form onSubmit={handleAddDay} className="card card-sm">
              <p style={{ fontWeight: 600, marginBottom: 10 }}>Day {days.length + 1}</p>
              <div className="form-group">
                <input className="form-input" placeholder="Title (optional)" value={newDay.title}
                  onChange={e => setNewDay(n => ({ ...n, title: e.target.value }))} />
              </div>
              <div className="form-group">
                <input type="date" className="form-input" value={newDay.dayDate}
                  onChange={e => setNewDay(n => ({ ...n, dayDate: e.target.value }))} />
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                <button type="button" className="btn btn-ghost btn-sm" onClick={() => setShowAddDay(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary btn-sm" disabled={addingDay}>
                  {addingDay ? 'Adding…' : 'Add Day'}
                </button>
              </div>
            </form>
          ) : (
            <button className="btn btn-ghost" style={{ border: '2px dashed var(--border)', width: '100%', padding: 20, borderRadius: 'var(--radius)', color: 'var(--text-light)' }}
              onClick={() => setShowAddDay(true)}>
              + Add Day
            </button>
          )}
        </div>
      </div>

      {days.length === 0 && (
        <div className="empty-state">
          <span className="emoji">🗓️</span>
          <p style={{ fontWeight: 600, marginBottom: 8 }}>No days planned yet</p>
          <p>Add your first day to start building the itinerary</p>
        </div>
      )}
    </div>
  )
}
