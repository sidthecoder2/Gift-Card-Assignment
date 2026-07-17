import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'

export default function GiftCardDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [card, setCard] = useState(null)
  const [selectedDenom, setSelectedDenom] = useState(null)
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [placing, setPlacing] = useState(false)

  useEffect(() => {
    setLoading(true)
    api.getGiftCard(id)
      .then(setCard)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  const handlePlaceOrder = async () => {
    if (!selectedDenom || !email) return
    setPlacing(true)
    try {
      const order = await api.placeOrder({
        giftCardId: Number(id),
        denomination: selectedDenom,
        customerEmail: email,
      })
      navigate(`/orders/${order.orderId}/confirmation`)
    } catch (e) {
      setError(e.message)
    } finally {
      setPlacing(false)
    }
  }

  if (loading) return <p className="page">Loading...</p>
  if (error) return <p className="page" style={{ color: 'var(--color-failed)' }}>Error: {error}</p>
  if (!card) return null

  return (
    <div className="page-narrow">
      <div className="detail-hero">
        <img src={card.imageUrl} alt={card.title} />
      </div>
      <h1 style={{ marginBottom: 4 }}>{card.title}</h1>
      <span className="gift-card-category">{card.category}</span>
      <p style={{ marginTop: 14 }}>{card.description}</p>
      <p style={{ color: 'var(--color-text-muted)', fontSize: 13 }}><em>{card.terms}</em></p>
      <p style={{ color: 'var(--color-text-muted)', fontSize: 13 }}>Valid for {card.validityDays} days</p>

      <h3 style={{ marginBottom: 6 }}>Select denomination</h3>
      <div className="denom-row">
        {card.denominations.map((d) => (
          <button
            key={d.denomination}
            className={`denom-btn ${selectedDenom === d.denomination ? 'selected' : ''}`}
            onClick={() => setSelectedDenom(d.denomination)}
          >
            ₹{d.denomination} <span style={{ opacity: 0.6, fontWeight: 400 }}>(₹{d.price})</span>
          </button>
        ))}
      </div>

      <div style={{ margin: '16px 0' }}>
        <input
          type="email"
          placeholder="your email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          style={{ width: '100%' }}
        />
      </div>

      <button disabled={!selectedDenom || !email || placing} onClick={handlePlaceOrder}>
        {placing ? 'Placing order...' : 'Place Order'}
      </button>
    </div>
  )
}
