import axiosClient from './axiosClient';
import { ENDPOINTS } from './apiEndpoints';

const getOrCreateWebToken = () => {
  const key = 'nexahr_push_token';
  let token = localStorage.getItem(key);
  if (!token) {
    token = `web-${crypto.randomUUID?.() || Date.now()}`;
    localStorage.setItem(key, token);
  }
  return token;
};

export const registerWebPush = async () => {
  try {
    const token = getOrCreateWebToken();
    await axiosClient.post(ENDPOINTS.PUSH.REGISTER, {
      deviceToken: token,
      platform: 'WEB',
    });
  } catch {
    // Push registration is best-effort
  }
};

export const unregisterWebPush = async () => {
  const token = localStorage.getItem('nexahr_push_token');
  if (!token) return;
  try {
    await axiosClient.delete(ENDPOINTS.PUSH.UNREGISTER, { params: { deviceToken: token } });
  } catch {
    // ignore
  }
};
