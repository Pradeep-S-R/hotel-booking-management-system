import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../AuthContext.jsx';
import RoomRow from '../components/RoomRow.jsx';
import BookingDialog from '../components/BookingDialog.jsx';
import { addDays, fmtDate, nightsBetween, today } from '../utils.js';

export default function Home() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ checkIn: today(), checkOut: addDays(today(), 1), guests: 1 });
  const [applied, setApplied] = useState(null); // search that produced the current list
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [confirmation, setConfirmation] = useState(null);

  const load = useCallback(async (search) => {
    setLoading(true);
    setError('');
    try {
      const path = search
        ? `/rooms/available?checkIn=${search.checkIn}&checkOut=${search.checkOut}&guests=${search.guests}`
        : '/rooms';
      setRooms(await api(path));
    } catch (err) {
      setError(err.message);
      setRooms([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(null);
  }, [load]);

  const update = (key, value) => setForm((f) => ({ ...f, [key]: value }));

  const changeCheckIn = (value) => {
    setForm((f) => ({
      ...f,
      checkIn: value,
      checkOut: nightsBetween(value, f.checkOut) < 1 ? addDays(value, 1) : f.checkOut
    }));
  };

  const search = (e) => {
    e.preventDefault();
    const s = { checkIn: form.checkIn, checkOut: form.checkOut, guests: Number(form.guests) || 1 };
    setApplied(s);
    setConfirmation(null);
    load(s);
  };

  const clear = () => {
    setApplied(null);
    load(null);
  };

  const book = (room) => {
    if (!user) {
      navigate('/login', { state: { from: '/' } });
      return;
    }
    setSelected(room);
  };

  const booked = (booking) => {
    setSelected(null);
    setConfirmation(booking);
    load(applied);
  };

  const nights = applied ? nightsBetween(applied.checkIn, applied.checkOut) : 0;

  return (
    <>
      <section className="hero">
        <h1>Find a room for your dates</h1>
        <p>Choose when you are staying and how many guests are travelling. We will show only the rooms that are free.</p>

        <form className="search" onSubmit={search}>
          <label className="field">
            <span>Check-in</span>
            <input type="date" min={today()} value={form.checkIn} onChange={(e) => changeCheckIn(e.target.value)} required />
          </label>
          <label className="field">
            <span>Check-out</span>
            <input type="date" min={addDays(form.checkIn, 1)} value={form.checkOut} onChange={(e) => update('checkOut', e.target.value)} required />
          </label>
          <label className="field">
            <span>Guests</span>
            <input type="number" min="1" max="10" value={form.guests} onChange={(e) => update('guests', e.target.value)} required />
          </label>
          <button className="btn btn-primary" type="submit">Check availability</button>
        </form>
      </section>

      {confirmation && (
        <div className="alert alert-ok" role="status">
          Room {confirmation.roomNumber} is booked from {fmtDate(confirmation.checkIn)} to {fmtDate(confirmation.checkOut)}.
          {' '}<Link to="/bookings">View your bookings</Link>
        </div>
      )}

      <section aria-live="polite">
        <div className="list-head">
          <h2>
            {applied
              ? `${rooms.length} ${rooms.length === 1 ? 'room' : 'rooms'} free, ${fmtDate(applied.checkIn)} to ${fmtDate(applied.checkOut)}`
              : 'All rooms'}
          </h2>
          {applied && <button className="btn btn-quiet" onClick={clear}>Show all rooms</button>}
        </div>

        {loading && <p className="muted">Loading rooms...</p>}
        {error && <p className="alert alert-error" role="alert">{error}</p>}
        {!loading && !error && rooms.length === 0 && (
          <div className="empty">
            <h3>No rooms free for those dates</h3>
            <p>Try different dates or fewer guests.</p>
          </div>
        )}

        <div className="rooms">
          {rooms.map((room) => (
            <RoomRow key={room.id} room={room} nights={nights} onBook={book} />
          ))}
        </div>
      </section>

      {selected && (
        <BookingDialog
          room={selected}
          initial={applied || {}}
          onClose={() => setSelected(null)}
          onBooked={booked}
        />
      )}
    </>
  );
}
