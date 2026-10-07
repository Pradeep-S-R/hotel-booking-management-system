import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { fmtDate, inr, titleCase, today } from '../utils.js';

export default function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      setBookings(await api('/bookings/my'));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const cancel = async (b) => {
    if (!window.confirm(`Cancel your booking for room ${b.roomNumber}?`)) return;
    setError('');
    try {
      await api(`/bookings/${b.id}/cancel`, { method: 'PATCH' });
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section>
      <h1>My bookings</h1>
      {loading && <p className="muted">Loading your bookings...</p>}
      {error && <p className="alert alert-error" role="alert">{error}</p>}

      {!loading && bookings.length === 0 && (
        <div className="empty">
          <h3>No bookings yet</h3>
          <p>When you book a room it will appear here. <Link to="/">Browse rooms</Link></p>
        </div>
      )}

      <div className="table-wrap">
        {bookings.length > 0 && (
          <table>
            <thead>
              <tr><th>Room</th><th>Stay</th><th>Guests</th><th>Total</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.id}>
                  <td>{titleCase(b.roomType)} {b.roomNumber}</td>
                  <td>{fmtDate(b.checkIn)} to {fmtDate(b.checkOut)} ({b.nights} {b.nights === 1 ? 'night' : 'nights'})</td>
                  <td>{b.guests}</td>
                  <td>{inr(b.totalPrice)}</td>
                  <td><span className={`status status-${b.status.toLowerCase()}`}>{titleCase(b.status)}</span></td>
                  <td className="right">
                    {b.status === 'CONFIRMED' && b.checkIn > today() && (
                      <button className="btn btn-quiet" onClick={() => cancel(b)}>Cancel</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
