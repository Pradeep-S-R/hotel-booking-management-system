import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext.jsx';
import { BRAND } from '../utils.js';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="nav">
      <div className="container nav-inner">
        <Link to="/" className="brand">{BRAND}</Link>
        <nav className="nav-links" aria-label="Main">
          <NavLink to="/" end>Rooms</NavLink>
          {user && <NavLink to="/bookings">My bookings</NavLink>}
          {isAdmin && <NavLink to="/admin">Manage hotel</NavLink>}
          {user ? (
            <>
              <span className="nav-user">{user.fullName}</span>
              <button className="btn btn-quiet" onClick={signOut}>Sign out</button>
            </>
          ) : (
            <>
              <NavLink to="/login">Sign in</NavLink>
              <Link to="/register" className="btn btn-primary btn-small">Create account</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
