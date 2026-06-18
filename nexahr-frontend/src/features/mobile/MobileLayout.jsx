import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Home, Clock, CalendarDays, DollarSign, Settings } from 'lucide-react';
import { useI18n } from '../../hooks/useI18n';

const tabs = [
  { key: '/mobile', icon: Home, labelKey: 'mobile.home' },
  { key: '/mobile/attendance', icon: Clock, labelKey: 'mobile.checkIn' },
  { key: '/mobile/leaves', icon: CalendarDays, labelKey: 'mobile.leaves' },
  { key: '/mobile/payroll', icon: DollarSign, labelKey: 'mobile.payroll' },
  { key: '/mobile/more', icon: Settings, labelKey: 'mobile.more' },
];

const MobileLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useI18n();

  const activeKey = tabs
    .map((tab) => tab.key)
    .filter((k) => location.pathname === k || (k !== '/mobile' && location.pathname.startsWith(k)))
    .sort((a, b) => b.length - a.length)[0] || '/mobile';

  return (
    <div className="mobile-shell">
      <div className="mobile-header">
        <span className="mobile-brand">NexaHR</span>
        <span className="mobile-tagline">Mobile</span>
      </div>
      <main className="mobile-content">
        <Outlet />
      </main>
      <nav className="mobile-tabbar">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const active = activeKey === tab.key;
          return (
            <button
              key={tab.key}
              type="button"
              className={`mobile-tab ${active ? 'mobile-tab-active' : ''}`}
              onClick={() => navigate(tab.key)}
            >
              <Icon size={22} />
              <span>{t(tab.labelKey)}</span>
            </button>
          );
        })}
      </nav>
    </div>
  );
};

export default MobileLayout;
