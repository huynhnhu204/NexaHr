import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';

const authService = {
  login: async (credentials) => {
    const response = await axiosClient.post(ENDPOINTS.AUTH.LOGIN, credentials);
    const data = response?.data ?? response;
    if (!data?.accessToken) {
      throw new Error(response?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra email và mật khẩu.');
    }
    return data;
  },

  register: async (payload) => {
    const response = await axiosClient.post(ENDPOINTS.AUTH.REGISTER, payload);
    return response?.data ?? response;
  },

  getCurrentUser: async () => {
    const response = await axiosClient.get(ENDPOINTS.AUTH.ME);
    return response?.data ?? response;
  },

  refreshToken: async (refreshToken) => {
    const response = await axiosClient.post(ENDPOINTS.AUTH.REFRESH_TOKEN, { refreshToken });
    return response?.data ?? response;
  },

  changePassword: async (payload) => {
    const response = await axiosClient.post(ENDPOINTS.AUTH.CHANGE_PASSWORD, payload);
    return response?.data ?? response;
  },

  forgotPassword: async (email) => {
    const response = await axiosClient.post(ENDPOINTS.AUTH.FORGOT_PASSWORD, { email });
    return response?.data ?? response;
  },

  resetPassword: async (payload) => {
    const response = await axiosClient.post(ENDPOINTS.AUTH.RESET_PASSWORD, payload);
    return response?.data ?? response;
  },

  logout: async (refreshToken) => {
    try {
      await axiosClient.post(ENDPOINTS.AUTH.LOGOUT, refreshToken ? { refreshToken } : {});
    } catch {
      // ignore logout API errors
    }
  },

  switchCompany: async (companyId) => {
    const response = await axiosClient.post(ENDPOINTS.COMPANIES.SWITCH, { companyId });
    return response?.data ?? response;
  },

  getGoogleConfig: async () => {
    const response = await axiosClient.get(ENDPOINTS.AUTH.GOOGLE_CONFIG);
    return response?.data ?? response;
  },

  loginWithGoogle: async (idToken) => {
    const response = await axiosClient.post(ENDPOINTS.AUTH.GOOGLE, { idToken });
    const data = response?.data ?? response;
    if (!data?.accessToken) {
      throw new Error(response?.message || 'Đăng nhập Google thất bại');
    }
    return data;
  },
};

export default authService;
