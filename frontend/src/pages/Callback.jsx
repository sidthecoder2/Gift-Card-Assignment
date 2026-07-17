import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function Callback() {
  const navigate = useNavigate()
  const [error, setError] = useState(null)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const code = params.get('code')
    const state = params.get('state')

    if (!code || state !== sessionStorage.getItem('oauth_state')) {
      setError('Invalid OAuth callback (missing code or state mismatch)')
      return
    }

    fetch('http://localhost:8080/api/auth/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    })
      .then((res) => {
        if (!res.ok) throw new Error(`Token exchange failed: ${res.status}`)
        return res.json()
      })
      .then((data) => {
        if (!data.access_token) throw new Error('No access_token in response')
        localStorage.setItem('access_token', data.access_token)
        navigate('/home')
      })
      .catch((e) => setError(e.message))
  }, [])

  if (error) return <p style={{ padding: 24, color: 'red' }}>{error}</p>
  return <p style={{ padding: 24 }}>Signing you in...</p>
}
