const API = process.env.E2E_API_URL || 'http://localhost:8080/api';
const FRONTEND = process.env.E2E_BASE_URL || 'http://localhost:5173';

export async function loginViaApi(email, password, companyId) {
  const body = { email, password };
  if (companyId) body.companyId = companyId;

  const res = await fetch(`${API}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    const err = await res.text();
    throw new Error(`Login failed (${res.status}): ${err}`);
  }

  const json = await res.json();
  return json.data ?? json;
}

export async function injectSession(page, session) {
  await page.goto(`${FRONTEND}/login`);
  await page.evaluate((payload) => {
    localStorage.setItem('token', payload.accessToken);
    localStorage.setItem('refreshToken', payload.refreshToken || '');
    localStorage.setItem('user', JSON.stringify(payload.user || {}));
    localStorage.setItem('tokenExpiry', String(Date.now() + 55 * 60 * 1000));
  }, session);
}

export async function loginAs(page, email = 'admin@nexahr.com', password = '123456', companyId = 9) {
  const session = await loginViaApi(email, password, companyId);
  await injectSession(page, session);
  return session;
}

export { API, FRONTEND };
