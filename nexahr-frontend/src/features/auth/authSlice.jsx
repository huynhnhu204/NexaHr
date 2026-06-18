import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import authService from './authService';
import { clearPermissionCache } from '../../hooks/usePermissions';

const storedUser = JSON.parse(localStorage.getItem('user') || 'null');
const storedToken = localStorage.getItem('token');
const storedRefreshToken = localStorage.getItem('refreshToken');

const persistTokens = (accessToken, refreshToken) => {
  localStorage.setItem('token', accessToken);
  if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
  localStorage.setItem('tokenExpiry', String(Date.now() + 55 * 60 * 1000));
};

export const login = createAsyncThunk('auth/login', async (credentials, { rejectWithValue }) => {
  try {
    return await authService.login(credentials);
  } catch (error) {
    return rejectWithValue(error.message);
  }
});

export const loginWithGoogle = createAsyncThunk('auth/google', async (idToken, { rejectWithValue }) => {
  try {
    return await authService.loginWithGoogle(idToken);
  } catch (error) {
    return rejectWithValue(error.message);
  }
});

export const getCurrentUser = createAsyncThunk('auth/me', async (_, { rejectWithValue }) => {
  try {
    return await authService.getCurrentUser();
  } catch (error) {
    return rejectWithValue(error.message);
  }
});

export const refreshToken = createAsyncThunk('auth/refresh', async (_, { getState, rejectWithValue }) => {
  try {
    const token = getState().auth.refreshToken || localStorage.getItem('refreshToken');
    if (!token) throw new Error('Không có refresh token');
    return await authService.refreshToken(token);
  } catch (error) {
    return rejectWithValue(error.message);
  }
});

export const logoutUser = createAsyncThunk('auth/logout', async (_, { getState }) => {
  const refreshTokenValue = getState().auth.refreshToken || localStorage.getItem('refreshToken');
  await authService.logout(refreshTokenValue);
});

export const switchCompany = createAsyncThunk('auth/switchCompany', async (companyId, { rejectWithValue }) => {
  try {
    return await authService.switchCompany(companyId);
  } catch (error) {
    return rejectWithValue(error.message);
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: storedUser,
    token: storedToken,
    refreshToken: storedRefreshToken,
    company: storedUser?.companyId ? { id: storedUser.companyId, name: storedUser.companyName } : null,
    isAuthenticated: !!storedToken,
    loading: false,
    error: null,
  },
  reducers: {
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.refreshToken = null;
      state.company = null;
      state.isAuthenticated = false;
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      localStorage.removeItem('tokenExpiry');
      clearPermissionCache();
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        if (!action.payload?.accessToken) {
          state.error = 'Phản hồi đăng nhập không hợp lệ';
          return;
        }
        state.isAuthenticated = true;
        state.token = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken || null;
        state.user = action.payload;
        state.company = action.payload.companyId
          ? { id: action.payload.companyId, name: action.payload.companyName }
          : null;
        persistTokens(action.payload.accessToken, action.payload.refreshToken);
        localStorage.setItem('user', JSON.stringify(action.payload));
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(loginWithGoogle.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginWithGoogle.fulfilled, (state, action) => {
        state.loading = false;
        if (!action.payload?.accessToken) {
          state.error = 'Phản hồi đăng nhập không hợp lệ';
          return;
        }
        state.isAuthenticated = true;
        state.token = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken || null;
        state.user = action.payload;
        state.company = action.payload.companyId
          ? { id: action.payload.companyId, name: action.payload.companyName }
          : null;
        persistTokens(action.payload.accessToken, action.payload.refreshToken);
        localStorage.setItem('user', JSON.stringify(action.payload));
      })
      .addCase(loginWithGoogle.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(getCurrentUser.fulfilled, (state, action) => {
        state.user = { ...state.user, ...action.payload };
        localStorage.setItem('user', JSON.stringify(state.user));
      })
      .addCase(refreshToken.fulfilled, (state, action) => {
        if (action.payload?.accessToken) {
          state.token = action.payload.accessToken;
          state.refreshToken = action.payload.refreshToken || state.refreshToken;
          persistTokens(action.payload.accessToken, action.payload.refreshToken);
        }
      })
      .addCase(switchCompany.fulfilled, (state, action) => {
        if (action.payload?.accessToken) {
          state.token = action.payload.accessToken;
          state.refreshToken = action.payload.refreshToken || state.refreshToken;
          state.user = { ...state.user, ...action.payload };
          state.company = action.payload.companyId
            ? { id: action.payload.companyId, name: action.payload.companyName }
            : state.company;
          persistTokens(action.payload.accessToken, action.payload.refreshToken);
          localStorage.setItem('user', JSON.stringify(state.user));
        }
      })
      .addCase(logoutUser.fulfilled, (state) => {
        state.user = null;
        state.token = null;
        state.refreshToken = null;
        state.company = null;
        state.isAuthenticated = false;
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        localStorage.removeItem('tokenExpiry');
      });
  },
});

export const { logout, clearError } = authSlice.actions;
export default authSlice.reducer;
