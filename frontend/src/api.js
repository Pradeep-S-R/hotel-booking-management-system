export const TOKEN_KEY = 'hbms.token';
export const USER_KEY = 'hbms.user';

export async function api(path, { method = 'GET', body } = {}) {
  const headers = {};
  const token = localStorage.getItem(TOKEN_KEY);
  if (body) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`/api${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined
    });
  } catch {
    throw new Error('Cannot reach the server. Check that the backend is running on port 8080.');
  }

  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);

  if (!res.ok) {
    if (res.status === 401 && token) {
      window.dispatchEvent(new Event('hbms:unauthorized'));
    }
    const error = new Error(data?.message || 'Something went wrong. Please try again.');
    error.status = res.status;
    throw error;
  }
  return data;
}
