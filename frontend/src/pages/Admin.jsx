import { useCallback, useEffect, useState } from 'react';
import { api } from '../api.js';
import { fmtDate, inr, titleCase } from '../utils.js';

const TYPES = ['STANDARD', 'DELUXE', 'FAMILY', 'SUITE'];
const EMPTY = { roomNumber: '', type: 'STANDARD', pricePerNight: '', capacity: 2, description: '', amenities: '', active: true };

export default function Admin() {
  const [tab, setTab] = useState('overview');
  const [stats, setStats] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [form, setForm] = useState(EMPTY);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const load = useCallback(async () => {
    try {
      const [s, r, b] = await Promise.all([api('/admin/stats'), api('/admin/rooms'), api('/admin/bookings')]);
      setStats(s);
      setRooms(r);
      setBookings(b);
    } catch (err) {
      setError(err.message);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));

  const startEdit = (room) => {
    setEditingId(room.id);
    setForm({
      roomNumber: room.roomNumber,
      type: room.type,
      pricePerNight: room.pricePerNight,
      capacity: room.capacity,
      description: room.description || '',
      amenities: room.amenities.join(', '),
      active: room.active
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const reset = () => {
    setEditingId(null);
    setForm(EMPTY);
  };

  const saveRoom = async (e) => {
    e.preventDefault();
    setError('');
    setNotice('');
    const body = {
      ...form,
      pricePerNight: Number(form.pricePerNight),
      capacity: Number(form.capacity),
      amenities: form.amenities.split(',').map((a) => a.trim()).filter(Boolean).join(',')
    };
    try {
      if (editingId) await api(`/admin/rooms/${editingId}`, { method: 'PUT', body });
      else await api('/admin/rooms', { method: 'POST', body });
      setNotice(editingId ? 'Room updated.' : 'Room added.');
      reset();
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const removeRoom = async (room) => {
    if (!window.confirm(`Delete room ${room.roomNumber}?`)) return;
    setError('');
    setNotice('');
    try {
      await api(`/admin/rooms/${room.id}`, { method: 'DELETE' });
      setNotice('Room deleted.');
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const cancelBooking = async (b) => {
    if (!window.confirm(`Cancel booking #${b.id} for ${b.guestName}?`)) return;
    setError('');
    setNotice('');
    try {
      await api(`/admin/bookings/${b.id}/cancel`, { method: 'PATCH' });
      setNotice('Booking cancelled.');
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section>
      <h1>Manage hotel</h1>

      <div className="tabs" role="tablist">
        {[['overview', 'Overview'], ['rooms', 'Rooms'], ['bookings', 'Bookings']].map(([id, label]) => (
          <button key={id} role="tab" aria-selected={tab === id} className={tab === id ? 'tab active' : 'tab'} onClick={() => setTab(id)}>
            {label}
          </button>
        ))}
      </div>

      {error && <p className="alert alert-error" role="alert">{error}</p>}
      {notice && <p className="alert alert-ok" role="status">{notice}</p>}

      {tab === 'overview' && stats && (
        <div className="stats">
          <div className="stat"><div className="stat-value">{stats.totalRooms}</div><div className="stat-label">Rooms</div></div>
          <div className="stat"><div className="stat-value">{stats.confirmedBookings}</div><div className="stat-label">Confirmed bookings</div></div>
          <div className="stat"><div className="stat-value">{stats.cancelledBookings}</div><div className="stat-label">Cancelled bookings</div></div>
          <div className="stat"><div className="stat-value">{inr(stats.revenue)}</div><div className="stat-label">Revenue from confirmed bookings</div></div>
        </div>
      )}

      {tab === 'rooms' && (
        <>
          <form className="panel" onSubmit={saveRoom}>
            <h2>{editingId ? 'Edit room' : 'Add a room'}</h2>
            <div className="grid-form">
              <label className="field"><span>Room number</span>
                <input value={form.roomNumber} onChange={(e) => set('roomNumber', e.target.value)} required /></label>
              <label className="field"><span>Type</span>
                <select value={form.type} onChange={(e) => set('type', e.target.value)}>
                  {TYPES.map((t) => <option key={t} value={t}>{titleCase(t)}</option>)}
                </select></label>
              <label className="field"><span>Price per night (INR)</span>
                <input type="number" min="1" value={form.pricePerNight} onChange={(e) => set('pricePerNight', e.target.value)} required /></label>
              <label className="field"><span>Capacity</span>
                <input type="number" min="1" max="10" value={form.capacity} onChange={(e) => set('capacity', e.target.value)} required /></label>
              <label className="field wide"><span>Description</span>
                <input value={form.description} onChange={(e) => set('description', e.target.value)} /></label>
              <label className="field wide"><span>Amenities (separate with commas)</span>
                <input value={form.amenities} onChange={(e) => set('amenities', e.target.value)} /></label>
              <label className="check wide">
                <input type="checkbox" checked={form.active} onChange={(e) => set('active', e.target.checked)} />
                <span>Available for booking</span>
              </label>
            </div>
            <div className="dialog-actions">
              {editingId && <button type="button" className="btn btn-quiet" onClick={reset}>Cancel edit</button>}
              <button type="submit" className="btn btn-primary">{editingId ? 'Save changes' : 'Add room'}</button>
            </div>
          </form>

          <div className="table-wrap">
            <table>
              <thead><tr><th>Room</th><th>Type</th><th>Price</th><th>Sleeps</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {rooms.map((r) => (
                  <tr key={r.id}>
                    <td>{r.roomNumber}</td>
                    <td>{titleCase(r.type)}</td>
                    <td>{inr(r.pricePerNight)}</td>
                    <td>{r.capacity}</td>
                    <td><span className={`status ${r.active ? 'status-confirmed' : 'status-cancelled'}`}>{r.active ? 'Active' : 'Inactive'}</span></td>
                    <td className="right">
                      <button className="btn btn-quiet" onClick={() => startEdit(r)}>Edit</button>
                      <button className="btn btn-quiet btn-danger" onClick={() => removeRoom(r)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {tab === 'bookings' && (
        <div className="table-wrap">
          {bookings.length === 0 ? (
            <div className="empty"><h3>No bookings yet</h3><p>Reservations made by guests will show up here.</p></div>
          ) : (
            <table>
              <thead><tr><th>#</th><th>Guest</th><th>Room</th><th>Stay</th><th>Total</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {bookings.map((b) => (
                  <tr key={b.id}>
                    <td>{b.id}</td>
                    <td>{b.guestName}<div className="muted small">{b.guestEmail}</div></td>
                    <td>{b.roomNumber}</td>
                    <td>{fmtDate(b.checkIn)} to {fmtDate(b.checkOut)}</td>
                    <td>{inr(b.totalPrice)}</td>
                    <td><span className={`status status-${b.status.toLowerCase()}`}>{titleCase(b.status)}</span></td>
                    <td className="right">
                      {b.status === 'CONFIRMED' && <button className="btn btn-quiet btn-danger" onClick={() => cancelBooking(b)}>Cancel</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </section>
  );
}
