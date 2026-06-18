import { useEffect, useState } from 'react';
import { Card, List, Button, Modal, Form, Input, Switch, message, Tag } from 'antd';
import { Pin, Plus, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { usePermission } from '../../hooks/usePermission';
import { formatDateTime } from '../../utils/formatDate';

const AnnouncementsPage = () => {
  const { isAdmin, hasRole } = usePermission();
  const canManage = isAdmin || hasRole('ADMIN', 'HR');
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetch = () => {
    setLoading(true);
    axiosClient.get(ENDPOINTS.ANNOUNCEMENTS, { params: { size: 50 } })
      .then((res) => setItems(res.data?.content || []))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetch(); }, []);

  const onCreate = async (values) => {
    await axiosClient.post(ENDPOINTS.ANNOUNCEMENTS, values);
    message.success('Đăng thông báo thành công');
    setModalOpen(false);
    form.resetFields();
    fetch();
  };

  const onDelete = (id) => {
    Modal.confirm({
      title: 'Xóa thông báo?',
      okType: 'danger',
      onOk: async () => {
        await axiosClient.delete(`${ENDPOINTS.ANNOUNCEMENTS}/${id}`);
        message.success('Đã xóa');
        fetch();
      },
    });
  };

  return (
    <div>
      <PageHeader
        title="Bảng tin nội bộ"
        subtitle="Thông báo và cập nhật từ công ty"
        extra={canManage && (
          <Button type="primary" icon={<Plus size={16} />} onClick={() => setModalOpen(true)}>
            Đăng thông báo
          </Button>
        )}
      />

      <Card loading={loading}>
        <List
          dataSource={items}
          locale={{ emptyText: 'Chưa có thông báo' }}
          renderItem={(item) => (
            <List.Item
              actions={canManage ? [
                <Button key="del" type="text" danger icon={<Trash2 size={16} />} onClick={() => onDelete(item.id)} />,
              ] : []}
            >
              <List.Item.Meta
                title={
                  <span>
                    {item.pinned && <Pin size={14} style={{ marginRight: 6, color: '#F59E0B' }} />}
                    {item.title}
                    {item.pinned && <Tag color="gold" style={{ marginLeft: 8 }}>Ghim</Tag>}
                  </span>
                }
                description={
                  <>
                    <div style={{ marginBottom: 8, whiteSpace: 'pre-wrap' }}>{item.content}</div>
                    <span style={{ color: '#94A3B8', fontSize: 12 }}>
                      {item.authorName} · {formatDateTime(item.createdAt)}
                    </span>
                  </>
                }
              />
            </List.Item>
          )}
        />
      </Card>

      <Modal title="Đăng thông báo mới" open={modalOpen} onCancel={() => setModalOpen(false)} footer={null} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={onCreate} initialValues={{ published: true, pinned: false }}>
          <Form.Item name="title" label="Tiêu đề" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="content" label="Nội dung" rules={[{ required: true }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item name="pinned" label="Ghim lên đầu" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>Đăng</Button>
        </Form>
      </Modal>
    </div>
  );
};

export default AnnouncementsPage;
