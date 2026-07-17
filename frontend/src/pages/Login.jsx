// TODO: Fill in these values once you've created your Application in Casdoor's admin UI
const CASDOOR_BASE_URL = "http://localhost:8000";
const CLIENT_ID = "ca7dffdd24b1a85d3533";
const REDIRECT_URI = "http://localhost:5173/callback";
const APP_NAME = "giftcard-app"; // the Application name you created in Casdoor

export default function Login() {
  const handleLogin = () => {
    const state = crypto.randomUUID();
    sessionStorage.setItem("oauth_state", state);

    const authUrl =
      `${CASDOOR_BASE_URL}/login/oauth/authorize` +
      `?client_id=${CLIENT_ID}` +
      `&response_type=code` +
      `&redirect_uri=${encodeURIComponent(REDIRECT_URI)}` +
      `&scope=read` +
      `&state=${state}`;

    window.location.href = authUrl;
  };

  return (
    <div style={{ display: "grid", placeItems: "center", height: "100vh" }}>
      <div>
        <h1>Gift Card Platform</h1>
        <button onClick={handleLogin}>Login / Sign up with Casdoor</button>
      </div>
    </div>
  );
}
