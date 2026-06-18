import { useEffect, useState } from 'react';
import { Card, Button, message, Badge, Spin } from 'antd';
import { LogIn, LogOut, Bell, CalendarDays, ChevronRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { useI18n } from '../../hooks/useI18n';

const MobileHubPage = () => {
  const { t } = useI18n();
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const fetch = () => {
    axiosClient.get(ENDPOINTS.MOBILE.SUMMARY)
      .then((res) => setSummary(res.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetch(); }, []);

  const checkIn = async () => {
    setActionLoading(true);
    try {
      await axiosClient.post(ENDPOINTS.ATTENDANCE.CHECK_IN);
      message.success(t('mobile.checkIn') + ' ✓');
      fetch();
    } catch (err) {
      message.error(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const checkOut = async () => {
    setActionLoading(true);
    try {
      await axiosClient.post(ENDPOINTS.ATTENDANCE.CHECK_OUT);
      message.success(t('mobile.checkOut') + ' ✓');
      fetch();
    } catch (err) {
      message.error(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: 48 }}><Spin /></div>;

  return (
    <div className="mobile-hub">
      <div className="mobile-greeting">
        <h2>{t('mobile.greeting')}, {summary?.fullName?.split(' ').pop() || '!'}</h2>
        <p>{summary?.checkedInToday ? t('mobile.checkedIn') : t('mobile.notCheckedIn')}</p>
      </div>

      <Card className="mobile-action-card">
        <div className="mobile-check-actions">
          <Button
            type="primary"
            size="large"
            icon={<LogIn size={20} />}
            loading={actionLoading}
            disabled={summary?.checkedInToday}
            onClick={checkIn}
            block
          >
            {t('mobile.checkIn')}
          </Button>
          <Button
            size="large"
            icon={<LogOut size={20} />}
            loading={actionLoading}
            disabled={!summary?.checkedInToday || summary?.checkedOutToday}
            onClick={checkOut}
            block
          >
            {t('mobile.checkOut')}
          </Button>
        </div>
      </Card>

      <div className="mobile-quick-grid">
        <Card className="mobile-quick-card" onClick={() => navigate('/mobile/leaves')}>
          <CalendarDays size={24} color="#2563EB" />
          <div>
            <div className="mobile-quick-label">{t('mobile.leaves')}</div>
            <Badge count={summary?.pendingLeaves || 0} showZero color="#F59E0B" />
          </div>
          <ChevronRight size={18} className="mobile-chevron" />
        </Card>
        <Card className="mobile-quick-card" onClick={() => navigate('/settings')}>
          <Bell size={24} color="#8B5CF6" />
          <div>
            <div className="mobile-quick-label">{t('mobile.notifications')}</div>
            <Badge count={summary?.unreadNotifications || 0} showZero />
          </div>
          <ChevronRight size={18} className="mobile-chevron" />
        </Card>
      </div>
    </div>
  );
};

export default MobileHubPage;
