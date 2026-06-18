import axios from 'axios';
import { storage } from './storage';

const API_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080/api';

const client = axios.create({ baseURL: API_URL, timeout: 20000 });

let refreshPromise = null;

const unwrap = (res) => res?.data ?? res;

const persistSession = async (payload) => {
  if (!payload?.accessToken) return;
  await storage.setItem('token', payload.accessToken);
  if (payload.refreshToken) await storage.setItem('refreshToken', payload.refreshToken);
  await storage.setItem('session', JSON.stringify({
    userId: payload.userId,
    email: payload.email,
    fullName: payload.fullName,
    role: payload.role,
    companyId: payload.companyId,
    companyName: payload.companyName,
    employeeId: payload.employeeId,
  }));
};

client.interceptors.request.use(async (config) => {
  const token = await storage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status !== 401 || original._retry) {
      throw new Error(error.response?.data?.message || error.message || 'Lỗi kết nối');
    }
    original._retry = true;
    const refreshToken = await storage.getItem('refreshToken');
    if (!refreshToken) throw new Error('Phiên đăng nhập đã hết hạn');

    if (!refreshPromise) {
      refreshPromise = axios
        .post(`${API_URL}/auth/refresh-token`, { refreshToken })
        .then(async (res) => {
          const data = unwrap(res)?.data ?? unwrap(res);
          await persistSession(data);
          return data.accessToken;
        })
        .finally(() => { refreshPromise = null; });
    }

    const newToken = await refreshPromise;
    original.headers.Authorization = `Bearer ${newToken}`;
    return client(original);
  },
);

const api = {
  login: async (email, password, companyId) => {
    const body = { email, password };
    if (companyId) body.companyId = companyId;
    const res = await client.post('/auth/login', body);
    const payload = unwrap(res)?.data ?? unwrap(res);
    await persistSession(payload);
    return payload;
  },

  validateSession: async () => {
    const token = await storage.getItem('token');
    if (!token) return null;
    const res = await client.get('/auth/me');
    return unwrap(res)?.data ?? unwrap(res);
  },

  getSession: async () => {
    const raw = await storage.getItem('session');
    return raw ? JSON.parse(raw) : null;
  },

  getCompanies: async () => {
    const res = await client.get('/companies/my');
    return unwrap(res)?.data ?? unwrap(res) ?? [];
  },

  switchCompany: async (companyId) => {
    const res = await client.post('/companies/switch', { companyId });
    const data = unwrap(res)?.data ?? unwrap(res);
    await persistSession(data);
    return data;
  },

  getMe: async () => {
    const res = await client.get('/auth/me');
    return unwrap(res)?.data ?? unwrap(res);
  },

  getMobileSummary: async () => {
    const res = await client.get('/mobile/summary');
    return unwrap(res)?.data ?? unwrap(res);
  },

  getMyLeaves: async () => {
    const res = await client.get('/leaves/my', { params: { size: 20 } });
    return unwrap(res)?.data?.content ?? [];
  },

  createLeave: async (payload) => {
    const res = await client.post('/leaves', payload);
    return unwrap(res)?.data ?? unwrap(res);
  },

  getMyPayrolls: async () => {
    const res = await client.get('/payrolls/my', { params: { size: 12 } });
    return unwrap(res)?.data?.content ?? [];
  },

  checkIn: async () => client.post('/attendance/check-in').then(unwrap),
  checkOut: async () => client.post('/attendance/check-out').then(unwrap),

  getNotifications: async () => {
    const res = await client.get('/notifications', { params: { size: 30 } });
    return unwrap(res)?.data?.content ?? [];
  },

  markNotificationRead: async (id) => {
    const res = await client.put(`/notifications/${id}/read`);
    return unwrap(res)?.data ?? unwrap(res);
  },

  markAllNotificationsRead: async () => client.put('/notifications/read-all').then(unwrap),

  registerPush: async () => {
    const token = `expo_demo_${Date.now()}`;
    await storage.setItem('pushToken', token);
    return client.post('/push/register', { deviceToken: token, platform: 'ANDROID' });
  },

  logout: async () => {
    try { await client.post('/auth/logout'); } catch { /* ignore */ }
    await storage.multiRemove(['token', 'refreshToken', 'session', 'pushToken']);
  },
};

export default api;
