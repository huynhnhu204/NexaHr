import { Link } from 'react-router-dom';
import { Card, List } from 'antd';
import { LayoutDashboard, BarChart3, Settings, Monitor } from 'lucide-react';
import { useI18n } from '../../hooks/useI18n';

const MobileMorePage = () => {
  const { t } = useI18n();

  const items = [
    { icon: LayoutDashboard, label: t('nav.dashboard'), path: '/dashboard' },
    { icon: BarChart3, label: t('nav.analytics'), path: '/analytics' },
    { icon: Monitor, label: t('nav.reports'), path: '/reports' },
    { icon: Settings, label: t('nav.settings'), path: '/settings' },
  ];

  return (
    <Card title={t('mobile.more')} className="mobile-more-card">
      <List
        dataSource={items}
        renderItem={(item) => {
          const Icon = item.icon;
          return (
            <List.Item>
              <Link to={item.path} className="mobile-more-link">
                <Icon size={20} />
                <span>{item.label}</span>
              </Link>
            </List.Item>
          );
        }}
      />
    </Card>
  );
};

export default MobileMorePage;
