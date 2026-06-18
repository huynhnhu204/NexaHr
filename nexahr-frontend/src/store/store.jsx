import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import themeReducer from './themeSlice';
import localeReducer from './localeSlice';

const store = configureStore({
  reducer: {
    auth: authReducer,
    theme: themeReducer,
    locale: localeReducer,
  },
});

export default store;
