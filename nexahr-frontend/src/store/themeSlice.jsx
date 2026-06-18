import { createSlice } from '@reduxjs/toolkit';

const stored = localStorage.getItem('theme');
const initialMode = stored === 'dark' ? 'dark' : 'light';

const themeSlice = createSlice({
  name: 'theme',
  initialState: { mode: initialMode },
  reducers: {
    setTheme: (state, action) => {
      state.mode = action.payload;
      localStorage.setItem('theme', action.payload);
      document.documentElement.setAttribute('data-theme', action.payload);
    },
    toggleTheme: (state) => {
      state.mode = state.mode === 'dark' ? 'light' : 'dark';
      localStorage.setItem('theme', state.mode);
      document.documentElement.setAttribute('data-theme', state.mode);
    },
  },
});

export const { setTheme, toggleTheme } = themeSlice.actions;
export default themeSlice.reducer;
