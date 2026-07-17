import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout.jsx'
import Login from './pages/Login.jsx'
import Callback from './pages/Callback.jsx'
import Home from './pages/Home.jsx'
import GiftCardDetail from './pages/GiftCardDetail.jsx'
import OrderConfirmation from './pages/OrderConfirmation.jsx'
import OrderHistory from './pages/OrderHistory.jsx'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* No nav bar on login/callback - user isn't "in" the app yet */}
        <Route path="/" element={<Login />} />
        <Route path="/callback" element={<Callback />} />

        {/* Everything else gets the persistent nav bar */}
        <Route element={<Layout />}>
          <Route path="/home" element={<Home />} />
          <Route path="/gift-cards/:id" element={<GiftCardDetail />} />
          <Route path="/orders/:id/confirmation" element={<OrderConfirmation />} />
          <Route path="/orders" element={<OrderHistory />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
