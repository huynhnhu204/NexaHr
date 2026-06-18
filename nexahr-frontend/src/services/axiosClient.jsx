import axios from 'axios';
import { API_BASE_URL } from '../utils/constants';
import { ENDPOINTS } from './apiEndpoints';

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

const clearSession = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  localStorage.removeItem('tokenExpiry');
};

const redirectToLogin = () => {
  if (window.location.pathname !== '/login' && !window.location.pathname.startsWith('/forgot-password')
    && !window.location.pathname.startsWith('/reset-password')) {
    window.location.href = '/login?session=expired';
  }
};

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const expiry = localStorage.getItem('tokenExpiry');
  if (expiry && Date.now() > Number(expiry)) {
    config._sessionExpired = true;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config;
    const isAuthRequest = originalRequest?.url?.includes('/auth/login')
      || originalRequest?.url?.includes('/auth/register')
      || originalRequest?.url?.includes('/auth/forgot-password')
      || originalRequest?.url?.includes('/auth/reset-password');

    if (error.response?.status === 401 && !isAuthRequest && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        clearSession();
        redirectToLogin();
        return Promise.reject(new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'));
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return axiosClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const res = await axios.post(`${API_BASE_URL}${ENDPOINTS.AUTH.REFRESH_TOKEN}`, { refreshToken });
        const data = res.data?.data ?? res.data;
        const newToken = data?.accessToken;
        if (!newToken) throw new Error('Không thể làm mới phiên đăng nhập');

        localStorage.setItem('token', newToken);
        if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('tokenExpiry', String(Date.now() + 55 * 60 * 1000));

        axiosClient.defaults.headers.common.Authorization = `Bearer ${newToken}`;
        processQueue(null, newToken);
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearSession();
        redirectToLogin();
        return Promise.reject(new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'));
      } finally {
        isRefreshing = false;
      }
    }

    const msg = error.response?.data?.message || error.message || 'Đã xảy ra lỗi';
    if (error.code === 'ERR_NETWORK') {
      return Promise.reject(new Error('Không thể kết nối server. Vui lòng chạy backend trên cổng 8080.'));
    }
    return Promise.reject(new Error(msg));
  }
);

export default axiosClient;
