import { useSelector } from 'react-redux';
import { ConfigProvider, theme as antTheme } from 'antd';
import viVN from 'antd/locale/vi_VN';
import { designTokens } from '../../utils/constants';

const lightTheme = {
  token: {
    colorPrimary: designTokens.colors.primary,
    colorSuccess: designTokens.colors.success,
    colorWarning: designTokens.colors.warning,
    colorError: designTokens.colors.danger,
    borderRadius: designTokens.radius.md,
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
    colorBgLayout: designTokens.colors.background,
    colorBgContainer: designTokens.colors.surface,
  },
  components: {
    Menu: { darkItemBg: designTokens.colors.sidebarBg },
    Table: { headerBg: '#F8FAFC' },
  },
};

const darkTheme = {
  algorithm: antTheme.darkAlgorithm,
  token: {
    colorPrimary: '#3B82F6',
    colorSuccess: designTokens.colors.success,
    colorWarning: designTokens.colors.warning,
    colorError: designTokens.colors.danger,
    borderRadius: designTokens.radius.md,
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
    colorBgLayout: '#0B1220',
    colorBgContainer: '#111827',
    colorText: '#F1F5F9',
    colorTextSecondary: '#94A3B8',
    colorBorder: '#1E293B',
  },
  components: {
    Menu: { darkItemBg: '#0F172A' },
    Table: { headerBg: '#1E293B', colorBgContainer: '#111827' },
    Card: { colorBgContainer: '#111827' },
  },
};

const ThemeProvider = ({ children }) => {
  const mode = useSelector((state) => state.theme?.mode || 'light');
  const config = mode === 'dark' ? darkTheme : lightTheme;

  return (
    <ConfigProvider theme={config} locale={viVN}>
      {children}
    </ConfigProvider>
  );
};

export default ThemeProvider;
