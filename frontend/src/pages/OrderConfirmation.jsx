import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { api } from '../api/client.js'

export default function OrderConfirmation() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [cancelling, setCancelling] = useState(false)

  useEffect(() => {
    api.getOrder(id)
      .then(setOrder)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  const handleCancel = async () => {
    setCancelling(true)
    try {
      const updated = await api.cancelOrder(id)
      setOrder(updated)
    } catch (e) {
      setError(e.message)
    } finally {
      setCancelling(false)
    }
  }

  if (loading) return <p className="page">Loading order...</p>
  if (error) return <p className="page" style={{ color: 'var(--color-failed)' }}>Error: {error}</p>
  if (!order) return null

  const isCancellable = order.status === 'PROCESSING'

  return (
    <div className="page-narrow">
      <div style={{ textAlign: 'center', marginBottom: 8 }}>
        <div className="result-icon">
          {order.status === 'SUCCESS' && '🎉'}
          {order.status === 'FAILED' && '⚠️'}
          {order.status === 'CANCELLED' && '🚫'}
          {order.status === 'PROCESSING' && '⏳'}
        </div>
        <span className={`status-badge status-${order.status}`}>{order.status}</span>
      </div>

      {order.status === 'SUCCESS' && (
        <div className="voucher-card">
          <div className="voucher-label">Voucher Code</div>
          <div className="voucher-code">{order.voucherCode}</div>
          <div className="voucher-meta-row">
            <div>
              <div className="voucher-label">PIN</div>
              <div>{order.voucherPin}</div>
            </div>
            <div>
              <div className="voucher-label">Expires</div>
              <div>{order.expiryDate}</div>
            </div>
          </div>
        </div>
      )}

      {order.status === 'FAILED' && (
        <p style={{ textAlign: 'center', color: 'var(--color-failed)' }}>
          {order.failureMessage || 'Something went wrong fulfilling your order.'}
        </p>
      )}

      <div style={{ background: 'var(--color-card)', border: '1px solid var(--color-border)', borderRadius: 'var(--radius)', padding: 16, marginTop: 12 }}>
        <p style={{ margin: '4px 0' }}><strong>Order ID:</strong> {order.orderId}</p>
        <p style={{ margin: '4px 0' }}><strong>Gift Card:</strong> {order.giftCardTitle}</p>
        <p style={{ margin: '4px 0' }}><strong>Denomination:</strong> ₹{order.denomination}</p>
        {order.price && <p style={{ margin: '4px 0' }}><strong>Price:</strong> ₹{order.price}</p>}
      </div>

      {isCancellable && (
        <button onClick={handleCancel} disabled={cancelling} className="secondary" style={{ marginTop: 16 }}>
          {cancelling ? 'Cancelling...' : 'Cancel Order'}
        </button>
      )}

      <div style={{ marginTop: 20, textAlign: 'center' }}>
        <Link to="/orders">View order history →</Link>
      </div>
    </div>
  )
}
