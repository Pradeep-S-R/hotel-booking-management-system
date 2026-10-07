import { inr, titleCase } from '../utils.js';

export default function RoomRow({ room, nights, onBook }) {
  return (
    <article className="room">
      <div className={`room-tile tile-${room.type.toLowerCase()}`}>
        <span className="room-number">{room.roomNumber}</span>
        <span className="room-type">{titleCase(room.type)}</span>
      </div>

      <div className="room-body">
        <h3>{titleCase(room.type)} room {room.roomNumber}</h3>
        <p className="room-desc">{room.description}</p>
        <p className="room-meta">Sleeps {room.capacity}</p>
        <ul className="chips">
          {room.amenities.map((a) => <li key={a}>{a}</li>)}
        </ul>
      </div>

      <div className="room-side">
        <div className="price">{inr(room.pricePerNight)}<span> / night</span></div>
        {nights > 0 && <div className="price-total">{inr(room.pricePerNight * nights)} for {nights} {nights === 1 ? 'night' : 'nights'}</div>}
        <button className="btn btn-primary" onClick={() => onBook(room)}>Book this room</button>
      </div>
    </article>
  );
}
