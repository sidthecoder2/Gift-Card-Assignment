const API_BASE = 'http://localhost:8080/api';

function getToken() {
  // TODO: once Casdoor OAuth flow is wired up, store the access_token here
  // (e.g. in memory / React context) after the /callback exchange.
  return localStorage.getItem('access_token'); // NOTE: fine for an assignment demo;
  // for production you'd avoid localStorage for tokens (XSS risk) and use memory + refresh flow.
}

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${getToken()}`,
      ...options.headers,
    },
  });

  if (!res.ok) {
    const body = await res.text();
    throw new Error(`API error ${res.status}: ${body}`);
  }
  return res.json();
}

export const api = {
  listGiftCards: (category) => request(`/gift-cards${category ? `?category=${category}` : ''}`),
  getGiftCard: (id) => request(`/gift-cards/${id}`),
  placeOrder: (payload) => request('/orders', { method: 'POST', body: JSON.stringify(payload) }),
  getOrder: (id) => request(`/orders/${id}`),
  cancelOrder: (id) => request(`/orders/${id}/cancel`, { method: 'POST' }),
  getOrderHistory: () => request('/orders'),
};
