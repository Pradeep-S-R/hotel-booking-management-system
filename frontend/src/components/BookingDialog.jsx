import { useEffect, useRef, useState } from 'react';
import { api } from '../api.js';
import { addDays, fmtDate, inr, nightsBetween, titleCase, today } from '../utils.js';

export default function BookingDialog({ room, initial, onClose, onBooked }) {
  const [checkIn, setCheckIn] = useState(initial.checkIn || today());
  const [checkOut, setCheckOut] = useState(initial.checkOut || addDays(today(), 1));
  const [guests, setGuests] = useState(Math.min(initial.guests || 1, room.capacity));
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const firstField = useRef(null);

  const nights = nightsBetween(checkIn, checkOut);
  const total = nights > 0 ? nights * room.pricePerNight : 0;

  useEffect(() => {
    firstField.current?.focus();
    const onKey = (e) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  const changeCheckIn = (value) => {
    setCheckIn(value);
    if (nightsBetween(value, checkOut) < 1) setCheckOut(addDays(value, 1));
  };

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (nights < 1) {
      setError('Check-out must be after check-in.');
      return;
    }
    setSaving(true);
    try {
      const booking = await api('/bookings', {
        method: 'POST',
        body: { roomId: room.id, checkIn, checkOut, guests: Number(guests) }
      });
      onBooked(booking);
    } catch (err) {
      setError(err.message);
      setSaving(false);
    }
  };

  return (
    <div className="overlay" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <form className="dialog" role="dialog" aria-modal="true" aria-labelledby="dlg-title" onSubmit={submit}>
        <h2 id="dlg-title">Book {titleCase(room.type)} room {room.roomNumber}</h2>

        <div className="field-row">
          <label className="field">
            <span>Check-in</span>
            <input ref={firstField} type="date" min={today()} value={checkIn} onChange={(e) => changeCheckIn(e.target.value)} required />
          </label>
          <label className="field">
            <span>Check-out</span>
            <input type="date" min={addDays(checkIn, 1)} value={checkOut} onChange={(e) => setCheckOut(e.target.value)} required />
          </label>
        </div>

        <label className="field">
          <span>Guests (up to {room.capacity})</span>
          <input type="number" min="1" max={room.capacity} value={guests} onChange={(e) => setGuests(e.target.value)} required />
        </label>

        <dl className="summary">
          <div><dt>Dates</dt><dd>{nights > 0 ? `${fmtDate(checkIn)} to ${fmtDate(checkOut)}` : 'Pick valid dates'}</dd></div>
          <div><dt>Rate</dt><dd>{inr(room.pricePerNight)} x {Math.max(nights, 0)} {nights === 1 ? 'night' : 'nights'}</dd></div>
          <div className="summary-total"><dt>Total</dt><dd>{inr(total)}</dd></div>
        </dl>

        {error && <p className="alert alert-error" role="alert">{error}</p>}

        <div className="dialog-actions">
          <button type="button" className="btn btn-quiet" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Booking...' : 'Confirm booking'}</button>
        </div>
      </form>
    </div>
  );
}
