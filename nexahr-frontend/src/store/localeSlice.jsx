import { createSlice } from '@reduxjs/toolkit';

const stored = localStorage.getItem('locale') || 'vi';

const localeSlice = createSlice({
  name: 'locale',
  initialState: { lang: stored },
  reducers: {
    setLocale: (state, action) => {
      state.lang = action.payload;
      localStorage.setItem('locale', action.payload);
      document.documentElement.setAttribute('lang', action.payload);
    },
  },
});

export const { setLocale } = localeSlice.actions;
export default localeSlice.reducer;
