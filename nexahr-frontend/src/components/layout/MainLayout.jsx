import { useEffect, useState, useMemo } from 'react';
import { Menu, Badge, Dropdown, Avatar, Space, Button, Tooltip, Drawer } from 'antd';
import {
  LayoutDashboard, Users, Building2, Briefcase, Clock, CalendarDays,
  DollarSign, UserPlus, Target, Settings, Bell, LogOut, User, ChevronDown,
  Search, GraduationCap, Laptop, BarChart3, ScrollText, PanelLeftClose, PanelLeft,
  Menu as MenuIcon, Shield, Smartphone, LineChart, Sparkles, Megaphone,
} from 'lucide-react';
import ErrorBoundary from '../common/ErrorBoundary';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { logoutUser } from '../../features/auth/authSlice';
import { useAuth } from '../../hooks/useAuth';
import { usePermission } from '../../hooks/usePermission';
import { NAVIGATION, ROLE_LABELS } from '../../utils/constants';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { registerWebPush } from '../../services/pushService';
import GlobalSearch from '../common/GlobalSearch';
import NotificationDrawer from '../common/NotificationDrawer';
import ThemeToggle from '../common/ThemeToggle';
import LanguageToggle from '../common/LanguageToggle';
import CompanySwitcher from '../common/CompanySwitcher';
import OnboardingWizard from '../onboarding/OnboardingWizard';

const iconMap = {
  LayoutDashboard, Users, Building2, Briefcase, Clock, CalendarDays,
  DollarSign, UserPlus, Target, Settings, GraduationCap, Laptop, BarChart3, ScrollText, Shield, LineChart, Sparkles, Megaphone,
};

const buildMenuItems = (items, role) =>
  items
    .filter((item) => item.roles.includes(role))
    .map((item) => {
      const Icon = iconMap[item.icon];
      if (item.children) {
        const children = item.children
          .filter((c) => c.roles.includes(role))
          .map((c) => ({ key: c.key, label: c.label }));
        if (children.length === 0) return null;
        return {
          key: item.key,
          icon: Icon ? <Icon size={18} /> : null,
          label: item.label,
          children,
        };
      }
      return {
        key: item.key,
        icon: Icon ? <Icon size={18} /> : null,
        label: item.label,
      };
    })
    .filter(Boolean);

const getOpenKeys = (pathname) => {
  if (pathname.startsWith('/employees') || pathname.startsWith('/departments') || pathname.startsWith('/positions') || pathname.startsWith('/org-chart')) return ['hr'];
  if (pathname.startsWith('/attendance')) return ['attendance'];
  if (pathname.startsWith('/recruitment') || pathname.startsWith('/interviews')) return ['recruitment'];
  return [];
};

const SidebarContent = ({ collapsed, onNavigate, onCollapse }) => {
  const location = useLocation();
  const { role } = usePermission();
  const menuItems = useMemo(() => buildMenuItems(NAVIGATION, role), [role]);
  const [openKeys, setOpenKeys] = useState(getOpenKeys(location.pathname));

  useEffect(() => {
    setOpenKeys(getOpenKeys(location.pathname));
  }, [location.pathname]);

  const selectedKey = NAVIGATION.flatMap((n) => n.children || [n])
    .map((n) => n.key)
    .filter((k) => location.pathname === k || (k !== '/dashboard' && location.pathname.startsWith(k)))
    .sort((a, b) => b.length - a.length)[0] || location.pathname;

  return (
    <div className={`sidebar ${collapsed ? 'sidebar-collapsed' : ''}`}>
      <div className="sidebar-logo">
        <h2>{collapsed ? 'N' : 'NexaHR'}</h2>
        {!collapsed && <span>Work Smarter. Manage Better.</span>}
      </div>
      <div className="sidebar-menu">
        <Menu
          mode="inline"
          inlineCollapsed={collapsed}
          selectedKeys={[selectedKey]}
          openKeys={collapsed ? [] : openKeys}
          onOpenChange={setOpenKeys}
          items={menuItems}
          onClick={({ key }) => {
            if (!['hr', 'attendance', 'recruitment'].includes(key)) {
              onNavigate(key);
            }
          }}
        />
      </div>
      {onCollapse && (
        <div className="sidebar-footer">
          <Button type="text" className="sidebar-collapse-btn" onClick={onCollapse}>
            {collapsed ? <PanelLeft size={18} /> : <PanelLeftClose size={18} />}
          </Button>
        </div>
      )}
    </div>
  );
};

const Topbar = ({ onMenuClick }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [searchOpen, setSearchOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);

  useEffect(() => {
    registerWebPush();
    const fetchUnread = async () => {
      try {
        const res = await axiosClient.get(ENDPOINTS.NOTIFICATIONS.UNREAD);
        setUnreadCount(res.data?.count || 0);
      } catch { /* ignore */ }
    };
    fetchUnread();
    const interval = setInterval(fetchUnread, 60000);
    return () => clearInterval(interval);
  }, [notifOpen]);

  useEffect(() => {
    const handler = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') { e.preventDefault(); setSearchOpen(true); }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, []);

  const handleLogout = () => {
    dispatch(logoutUser()).then(() => navigate('/login'));
  };

  const userMenu = {
    items: [
      { key: 'profile', icon: <User size={16} />, label: 'Hồ sơ của tôi', onClick: () => navigate('/settings') },
      { key: 'password', icon: <Settings size={16} />, label: 'Đổi mật khẩu', onClick: () => navigate('/change-password') },
      { type: 'divider' },
      { key: 'logout', icon: <LogOut size={16} />, label: 'Đăng xuất', danger: true, onClick: handleLogout },
    ],
  };

  return (
    <>
      <div className="topbar">
        <div className="topbar-left">
          <Button type="text" className="mobile-menu-btn" icon={<MenuIcon size={22} />} onClick={onMenuClick} />
          <div className="global-search-trigger" onClick={() => setSearchOpen(true)}>
            <Search size={16} color="#94A3B8" />
            <span>Tìm kiếm toàn cục...</span>
            <kbd>⌘K</kbd>
          </div>
        </div>
        <div className="topbar-right">
          <CompanySwitcher />
          <Tooltip title="Mobile App">
            <Button type="text" icon={<Smartphone size={20} />} className="topbar-icon-btn" onClick={() => navigate('/mobile')} />
          </Tooltip>
          <LanguageToggle />
          <ThemeToggle />
          <Tooltip title="Thông báo">
            <Badge count={unreadCount} size="small">
              <Button type="text" icon={<Bell size={20} />} className="topbar-icon-btn" onClick={() => setNotifOpen(true)} />
            </Badge>
          </Tooltip>
          <Dropdown menu={userMenu} trigger={['click']}>
            <Space className="topbar-user" style={{ cursor: 'pointer' }}>
              <Avatar style={{ backgroundColor: '#1E3A8A' }}>
                {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
              </Avatar>
              <div className="topbar-user-info">
                <div className="topbar-user-name">{user?.fullName || user?.username}</div>
                <div className="topbar-user-role">{ROLE_LABELS[user?.role] || user?.role}</div>
              </div>
              <ChevronDown size={16} color="#64748b" className="topbar-chevron" />
            </Space>
          </Dropdown>
        </div>
      </div>
      <GlobalSearch open={searchOpen} onClose={() => setSearchOpen(false)} />
      <NotificationDrawer open={notifOpen} onClose={() => setNotifOpen(false)} />
    </>
  );
};

const MainLayout = () => {
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth <= 768);
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const handleNavigate = (key) => {
    navigate(key);
    if (isMobile) setMobileOpen(false);
  };

  return (
    <div className="main-layout">
      {!isMobile && (
        <SidebarContent
          collapsed={collapsed}
          onNavigate={handleNavigate}
          onCollapse={() => setCollapsed(!collapsed)}
        />
      )}

      <Drawer
        placement="left"
        open={isMobile && mobileOpen}
        onClose={() => setMobileOpen(false)}
        width={280}
        className="mobile-sidebar-drawer"
        styles={{ body: { padding: 0, background: '#0F172A' } }}
      >
        <SidebarContent collapsed={false} onNavigate={handleNavigate} />
      </Drawer>

      <div className={`main-content ${!isMobile && collapsed ? 'main-content-collapsed' : ''} ${isMobile ? 'main-content-mobile' : ''}`}>
        <Topbar onMenuClick={() => setMobileOpen(true)} />
        <OnboardingWizard />
        <div className="page-content">
          <ErrorBoundary>
            <Outlet />
          </ErrorBoundary>
        </div>
      </div>
    </div>
  );
};

export default MainLayout;
