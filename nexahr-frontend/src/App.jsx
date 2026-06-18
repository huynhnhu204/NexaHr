import { useEffect } from 'react';
import { Provider, useSelector } from 'react-redux';
import store from './store/store';
import AppRoutes from './routes/AppRoutes';
import ThemeProvider from './components/layout/ThemeProvider';
import './styles/global.css';

const ThemeInit = () => {
  const mode = useSelector((state) => state.theme?.mode || 'light');
  const lang = useSelector((state) => state.locale?.lang || 'vi');
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', mode);
  }, [mode]);
  useEffect(() => {
    document.documentElement.setAttribute('lang', lang);
  }, [lang]);
  return null;
};

function App() {
  return (
    <Provider store={store}>
      <ThemeInit />
      <ThemeProvider>
        <AppRoutes />
      </ThemeProvider>
    </Provider>
  );
}

export default App;
