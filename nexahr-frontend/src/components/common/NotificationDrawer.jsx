import { useEffect, useState } from 'react';
import { Drawer, List, Badge, Button, Empty, Spin } from 'antd';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { formatDateTime } from '../../utils/formatDate';

const NotificationDrawer = ({ open, onClose }) => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetch = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.NOTIFICATIONS.BASE, { params: { size: 30 } });
      setNotifications(res.data?.content || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { if (open) fetch(); }, [open]);

  const markAllRead = async () => {
    await axiosClient.put(ENDPOINTS.NOTIFICATIONS.READ_ALL);
    fetch();
  };

  return (
    <Drawer title="Trung tâm thông báo" open={open} onClose={onClose} width={400}
      extra={<Button size="small" onClick={markAllRead}>Đánh dấu đã đọc</Button>}>
      {loading ? <div style={{ textAlign: 'center', padding: 40 }}><Spin /></div> : (
        <List
          dataSource={notifications}
          locale={{ emptyText: <Empty description="Không có thông báo" /> }}
          renderItem={(item) => (
            <List.Item className={!item.isRead ? 'notification-unread' : ''}>
              <List.Item.Meta
                avatar={<Badge dot={!item.isRead} />}
                title={item.title}
                description={<><div>{item.message}</div><div className="notification-time">{formatDateTime(item.createdAt)}</div></>}
              />
            </List.Item>
          )}
        />
      )}
    </Drawer>
  );
};

export default NotificationDrawer;
