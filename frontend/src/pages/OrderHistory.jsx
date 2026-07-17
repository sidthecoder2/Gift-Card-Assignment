import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'

export default function OrderHistory() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.getOrderHistory()
      .then(setOrders)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="page">Loading order history...</p>
  if (error) return <p className="page" style={{ color: 'var(--color-failed)' }}>Error: {error}</p>

  return (
    <div className="page">
      <h1 className="page-title">Order History</h1>

      {orders.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📦</div>
          <p>No orders yet — go grab a gift card!</p>
        </div>
      ) : (
        <table className="order-table">
          <thead>
            <tr>
              <th>Gift Card</th>
              <th>Denomination</th>
              <th>Price</th>
              <th>Date</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.orderId} className="clickable" onClick={() => navigate(`/orders/${o.orderId}/confirmation`)}>
                <td>{o.giftCardTitle}</td>
                <td>₹{o.denomination}</td>
                <td>{o.price ? `₹${o.price}` : '-'}</td>
                <td>{new Date(o.createdAt).toLocaleDateString()}</td>
                <td><span className={`status-badge status-${o.status}`}>{o.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
