import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client.js";

export default function Home() {
  const [cards, setCards] = useState([]);
  const [category, setCategory] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    api
      .listGiftCards(category)
      .then(setCards)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [category]);

  return (
    <div className="page">
      <div className="hero-banner">
        <h2>Gift Cards, Delivered Instantly</h2>
        <p>Shop top brands and get your voucher code in seconds.</p>
      </div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          flexWrap: "wrap",
          gap: 12,
          marginBottom: 20,
        }}
      >
        <h1 className="page-title" style={{ marginBottom: 0 }}>
          Gift Cards
        </h1>
        {/* TODO: populate categories dynamically from backend instead of hardcoding */}
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">All categories</option>
          <option value="shopping">Shopping</option>
        </select>
      </div>

      {loading && <p>Loading gift cards...</p>}
      {error && (
        <p style={{ color: "var(--color-failed)" }}>
          Something went wrong: {error}
        </p>
      )}

      {!loading && !error && cards.length === 0 && (
        <div className="empty-state">
          <div className="empty-state-icon">🔍</div>
          <p>No gift cards found for this category.</p>
        </div>
      )}

      <div className="card-grid">
        {cards.map((card) => (
          <Link
            key={card.id}
            to={`/gift-cards/${card.id}`}
            className="gift-card"
          >
            <img src={card.imageUrl} alt={card.title} />
            <div className="gift-card-body">
              <p className="gift-card-title">{card.title}</p>
              <span className="gift-card-category">{card.category}</span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
