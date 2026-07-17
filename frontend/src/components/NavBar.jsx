import { Link, useNavigate } from 'react-router-dom'

export default function NavBar() {
  const navigate = useNavigate()

  const handleLogout = () => {
    localStorage.removeItem('access_token')
    navigate('/')
  }

  return (
    <nav className="navbar">
      <Link to="/home" className="navbar-brand">🎁 Gift Card Platform</Link>
      <div className="navbar-links">
        <Link to="/home">Home</Link>
        <Link to="/orders">Order History</Link>
        <button onClick={handleLogout} className="secondary">Logout</button>
      </div>
    </nav>
  )
}
