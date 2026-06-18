import { useEffect, useState } from 'react';
import { Card, Descriptions, List, Badge, Button, message, Segmented, Switch } from 'antd';
import { Link } from 'react-router-dom';
import {
  KeyRound, Moon, Sun, CreditCard, Building2, Plug, GitBranch, Shield, Database, UserCog, User,
} from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import PageHeader from '../../components/common/PageHeader';
import { useAuth } from '../../hooks/useAuth';
import { usePermission } from '../../hooks/usePermission';
import { setTheme } from '../../store/themeSlice';
import LanguageToggle from '../../components/common/LanguageToggle';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { ROLE_LABELS } from '../../utils/constants';
import { formatDateTime } from '../../utils/formatDate';

const ADMIN_LINKS = [
  { to: '/settings/subscription', icon: CreditCard, label: 'Gói đăng ký', desc: 'Gói và thanh toán' },
  { to: '/settings/data', icon: Database, label: 'Dữ liệu', desc: 'Import / export' },
  { to: '/settings/custom-roles', icon: UserCog, label: 'Vai trò', desc: 'Vai trò tùy chỉnh' },
  { to: '/settings/permissions', icon: Shield, label: 'Phân quyền', desc: 'Ma trận quyền' },
  { to: '/settings/workflows', icon: GitBranch, label: 'Quy trình', desc: 'Workflow tự động' },
  { to: '/settings/integrations', icon: Plug, label: 'Tích hợp', desc: 'API & webhook' },
  { to: '/settings/company', icon: Building2, label: 'Cài đặt công ty', desc: 'Thông tin công ty' },
];

const SettingsPage = () => {
  const { user } = useAuth();
  const { isAdmin } = usePermission();
  const dispatch = useDispatch();
  const themeMode = useSelector((state) => state.theme?.mode || 'light');
  const [notifications, setNotifications] = useState([]);
  const [emailPrefs, setEmailPrefs] = useState({
    notifyEmailLeave: true,
    notifyEmailPayroll: true,
    notifyEmailSystem: true,
  });
  const [prefsLoading, setPrefsLoading] = useState(false);

  useEffect(() => {
    axiosClient.get(ENDPOINTS.NOTIFICATIONS.BASE, { params: { size: 20 } })
      .then((res) => setNotifications(res.data?.content || []))
      .catch(() => {});
    axiosClient.get(ENDPOINTS.USER_PREFERENCES.NOTIFICATIONS)
      .then((res) => setEmailPrefs(res.data || {}))
      .catch(() => {});
  }, []);

  const markAllRead = async () => {
    try {
      await axiosClient.put(ENDPOINTS.NOTIFICATIONS.READ_ALL);
      message.success('Đã đánh dấu tất cả thông báo là đã đọc');
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (err) {
      message.error(err.message);
    }
  };

  const updateEmailPref = async (key, value) => {
    setPrefsLoading(true);
    const next = { ...emailPrefs, [key]: value };
    setEmailPrefs(next);
    try {
      await axiosClient.put(ENDPOINTS.USER_PREFERENCES.NOTIFICATIONS, next);
      message.success('Đã cập nhật tùy chọn email');
    } catch (err) {
      message.error(err.message);
      setEmailPrefs(emailPrefs);
    } finally {
      setPrefsLoading(false);
    }
  };

  return (
    <div>
      <PageHeader title="Cài đặt" subtitle="Thông tin tài khoản và thông báo" />

      <Card
        title={(
          <span className="settings-card-title">
            <User size={18} />
            Thông tin tài khoản
          </span>
        )}
        style={{ marginBottom: 24, borderRadius: 12 }}
        extra={(
          <Link to="/change-password">
            <Button icon={<KeyRound size={16} />}>Đổi mật khẩu</Button>
          </Link>
        )}
      >
        <Descriptions column={{ xs: 1, sm: 2 }}>
          <Descriptions.Item label="Họ và tên">{user?.fullName}</Descriptions.Item>
          <Descriptions.Item label="Email">{user?.email}</Descriptions.Item>
          <Descriptions.Item label="Tên đăng nhập">{user?.username}</Descriptions.Item>
          <Descriptions.Item label="Vai trò">{ROLE_LABELS[user?.role] || user?.role}</Descriptions.Item>
          {user?.companyName && (
            <Descriptions.Item label="Công ty">{user.companyName}</Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      {isAdmin && (
        <Card title="Quản trị hệ thống" style={{ marginBottom: 24, borderRadius: 12 }}>
          <div className="settings-admin-grid">
            {ADMIN_LINKS.map(({ to, icon: Icon, label, desc }) => (
              <Link key={to} to={to} className="settings-admin-link">
                <Icon size={20} />
                <div>
                  <div className="settings-admin-link-title">{label}</div>
                  <div className="settings-admin-link-desc">{desc}</div>
                </div>
              </Link>
            ))}
          </div>
        </Card>
      )}

      <Card title="Giao diện" style={{ marginBottom: 24, borderRadius: 12 }}>
        <div className="settings-option-row">
          <div className="settings-option-label">
            <div>Ngôn ngữ</div>
            <span>Tiếng Việt / English</span>
          </div>
          <div className="settings-option-control">
            <LanguageToggle size="middle" />
          </div>
        </div>
        <div className="settings-option-row">
          <div className="settings-option-label">
            <div>Chế độ hiển thị</div>
            <span>Chọn giao diện sáng hoặc tối</span>
          </div>
          <div className="settings-option-control">
            <Segmented
              value={themeMode}
              onChange={(v) => dispatch(setTheme(v))}
              options={[
                { label: 'Sáng', value: 'light', icon: <Sun size={14} /> },
                { label: 'Tối', value: 'dark', icon: <Moon size={14} /> },
              ]}
            />
          </div>
        </div>
      </Card>

      <Card title="Thông báo email" style={{ marginBottom: 24, borderRadius: 12 }}>
        {[
          { key: 'notifyEmailLeave', label: 'Nghỉ phép', desc: 'Email khi có yêu cầu nghỉ phép cần duyệt' },
          { key: 'notifyEmailPayroll', label: 'Lương', desc: 'Email khi bảng lương được phát hành' },
          { key: 'notifyEmailSystem', label: 'Hệ thống', desc: 'Email thông báo hệ thống quan trọng' },
        ].map((item) => (
          <div key={item.key} className="settings-option-row">
            <div className="settings-option-label">
              <div>{item.label}</div>
              <span>{item.desc}</span>
            </div>
            <div className="settings-option-control">
              <Switch
                checked={emailPrefs[item.key]}
                loading={prefsLoading}
                onChange={(v) => updateEmailPref(item.key, v)}
              />
            </div>
          </div>
        ))}
      </Card>

      <Card
        title="Thông báo"
        extra={<Button size="small" onClick={markAllRead}>Đánh dấu đã đọc tất cả</Button>}
        style={{ borderRadius: 12 }}
      >
        <List
          dataSource={notifications}
          locale={{ emptyText: 'Không có thông báo' }}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                avatar={<Badge dot={!item.isRead} />}
                title={item.title}
                description={item.message}
              />
              <span style={{ color: '#94a3b8', fontSize: 12 }}>{formatDateTime(item.createdAt)}</span>
            </List.Item>
          )}
        />
      </Card>
    </div>
  );
};

export default SettingsPage;
