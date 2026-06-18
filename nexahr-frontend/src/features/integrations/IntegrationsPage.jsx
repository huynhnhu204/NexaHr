import { useEffect, useState } from 'react';
import {
  Card, Tabs, Table, Button, Form, Input, Modal, message, Tag, Select, Switch, Alert, Space, Typography,
} from 'antd';
import { Key, Webhook, Shield, Copy, Trash2, Play, Plus } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { formatDateTime } from '../../utils/formatDate';

const { Text, Paragraph } = Typography;

const WEBHOOK_EVENTS = [
  { value: 'EMPLOYEE_CREATED', label: 'Nhân viên mới' },
  { value: 'LEAVE_APPROVED', label: 'Nghỉ phép được duyệt' },
  { value: 'LEAVE_REJECTED', label: 'Nghỉ phép bị từ chối' },
  { value: 'PAYROLL_APPROVED', label: 'Bảng lương được duyệt' },
];

const IntegrationsPage = () => {
  const [apiKeys, setApiKeys] = useState([]);
  const [webhooks, setWebhooks] = useState([]);
  const [deliveries, setDeliveries] = useState([]);
  const [saml, setSaml] = useState(null);
  const [loading, setLoading] = useState(true);
  const [keyModal, setKeyModal] = useState(false);
  const [webhookModal, setWebhookModal] = useState(false);
  const [newRawKey, setNewRawKey] = useState(null);
  const [keyForm] = Form.useForm();
  const [webhookForm] = Form.useForm();
  const [samlForm] = Form.useForm();

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [keysRes, hooksRes, delRes, samlRes] = await Promise.all([
        axiosClient.get(ENDPOINTS.INTEGRATIONS.API_KEYS),
        axiosClient.get(ENDPOINTS.INTEGRATIONS.WEBHOOKS),
        axiosClient.get(ENDPOINTS.INTEGRATIONS.WEBHOOK_DELIVERIES, { params: { size: 10 } }),
        axiosClient.get(ENDPOINTS.INTEGRATIONS.SAML),
      ]);
      setApiKeys(keysRes.data || []);
      setWebhooks(hooksRes.data || []);
      setDeliveries(delRes.data?.content || []);
      setSaml(samlRes.data);
      samlForm.setFieldsValue(samlRes.data || {});
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, [samlForm]);

  const createApiKey = async (values) => {
    const res = await axiosClient.post(ENDPOINTS.INTEGRATIONS.API_KEYS, values);
    setNewRawKey(res.data?.rawKey);
    setKeyModal(false);
    keyForm.resetFields();
    fetchAll();
    message.success('Tạo API key thành công');
  };

  const revokeKey = (id) => {
    Modal.confirm({
      title: 'Thu hồi API key?',
      content: 'Key sẽ không thể sử dụng sau khi thu hồi.',
      okText: 'Thu hồi',
      okType: 'danger',
      onOk: async () => {
        await axiosClient.delete(ENDPOINTS.INTEGRATIONS.API_KEY(id));
        message.success('Đã thu hồi API key');
        fetchAll();
      },
    });
  };

  const createWebhook = async (values) => {
    await axiosClient.post(ENDPOINTS.INTEGRATIONS.WEBHOOKS, values);
    setWebhookModal(false);
    webhookForm.resetFields();
    fetchAll();
    message.success('Tạo webhook thành công');
  };

  const deleteWebhook = (id) => {
    Modal.confirm({
      title: 'Xóa webhook?',
      okText: 'Xóa',
      okType: 'danger',
      onOk: async () => {
        await axiosClient.delete(ENDPOINTS.INTEGRATIONS.WEBHOOK(id));
        message.success('Đã xóa webhook');
        fetchAll();
      },
    });
  };

  const testWebhook = async (id) => {
    await axiosClient.post(ENDPOINTS.INTEGRATIONS.WEBHOOK_TEST(id));
    message.success('Đã gửi webhook test');
    fetchAll();
  };

  const saveSaml = async (values) => {
    const res = await axiosClient.put(ENDPOINTS.INTEGRATIONS.SAML, values);
    setSaml(res.data);
    message.success('Cập nhật SAML thành công');
  };

  const copyText = (text) => {
    navigator.clipboard.writeText(text);
    message.success('Đã sao chép');
  };

  const apiKeyColumns = [
    { title: 'Tên', dataIndex: 'name', key: 'name' },
    { title: 'Prefix', dataIndex: 'keyPrefix', key: 'keyPrefix' },
    { title: 'Quyền', dataIndex: 'scopes', key: 'scopes' },
    { title: 'Trạng thái', dataIndex: 'active', key: 'active', render: (v) => <Tag color={v ? 'green' : 'red'}>{v ? 'Hoạt động' : 'Đã thu hồi'}</Tag> },
    { title: 'Lần dùng cuối', dataIndex: 'lastUsedAt', key: 'lastUsedAt', render: (v) => v ? formatDateTime(v) : '—' },
    {
      title: '', key: 'actions', render: (_, row) => row.active && (
        <Button type="text" danger icon={<Trash2 size={16} />} onClick={() => revokeKey(row.id)} />
      ),
    },
  ];

  const webhookColumns = [
    { title: 'Tên', dataIndex: 'name', key: 'name' },
    { title: 'URL', dataIndex: 'url', key: 'url', ellipsis: true },
    { title: 'Sự kiện', dataIndex: 'events', key: 'events', render: (events) => (events || []).map((e) => <Tag key={e}>{e}</Tag>) },
    { title: 'Trạng thái', dataIndex: 'active', key: 'active', render: (v) => <Tag color={v ? 'green' : 'default'}>{v ? 'Hoạt động' : 'Đã tắt'}</Tag> },
    {
      title: '', key: 'actions', render: (_, row) => row.active && (
        <Space>
          <Button type="text" icon={<Play size={16} />} onClick={() => testWebhook(row.id)} title="Gửi test" />
          <Button type="text" danger icon={<Trash2 size={16} />} onClick={() => deleteWebhook(row.id)} />
        </Space>
      ),
    },
  ];

  const deliveryColumns = [
    { title: 'Webhook', dataIndex: 'webhookName', key: 'webhookName' },
    { title: 'Sự kiện', dataIndex: 'event', key: 'event' },
    { title: 'HTTP', dataIndex: 'statusCode', key: 'statusCode' },
    { title: 'Kết quả', dataIndex: 'success', key: 'success', render: (v) => <Tag color={v ? 'green' : 'red'}>{v ? 'Thành công' : 'Thất bại'}</Tag> },
    { title: 'Thời gian', dataIndex: 'attemptedAt', key: 'attemptedAt', render: (v) => formatDateTime(v) },
  ];

  const tabItems = [
    {
      key: 'api-keys',
      label: <span><Key size={14} style={{ marginRight: 6, verticalAlign: -2 }} />API Keys</span>,
      children: (
        <Card loading={loading}>
          <Alert
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
            message="API v1 — dùng header X-API-Key"
            description={
              <span>
                <code>GET /api/v1/employees</code> · <code>GET /api/v1/departments</code>
                {' '}(yêu cầu gói Pro hoặc Enterprise)
              </span>
            }
          />
          <Button type="primary" icon={<Plus size={16} />} onClick={() => setKeyModal(true)} style={{ marginBottom: 16 }}>
            Tạo API Key
          </Button>
          <Table rowKey="id" columns={apiKeyColumns} dataSource={apiKeys} pagination={false} locale={{ emptyText: 'Chưa có API key' }} />
        </Card>
      ),
    },
    {
      key: 'webhooks',
      label: <span><Webhook size={14} style={{ marginRight: 6, verticalAlign: -2 }} />Webhooks</span>,
      children: (
        <>
          <Card loading={loading} style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<Plus size={16} />} onClick={() => setWebhookModal(true)} style={{ marginBottom: 16 }}>
              Thêm Webhook
            </Button>
            <Table rowKey="id" columns={webhookColumns} dataSource={webhooks} pagination={false} locale={{ emptyText: 'Chưa có webhook' }} />
          </Card>
          <Card title="Lịch sử gửi" loading={loading}>
            <Table rowKey="id" columns={deliveryColumns} dataSource={deliveries} pagination={false} scroll={{ x: 600 }} />
          </Card>
        </>
      ),
    },
    {
      key: 'saml',
      label: <span><Shield size={14} style={{ marginRight: 6, verticalAlign: -2 }} />SAML SSO</span>,
      children: (
        <Card loading={loading}>
          {saml?.enterpriseRequired && (
            <Alert type="warning" showIcon style={{ marginBottom: 16 }}
              message="Yêu cầu gói Enterprise" description="Nâng cấp lên Enterprise để cấu hình SAML SSO cho doanh nghiệp." />
          )}
          <Form form={samlForm} layout="vertical" onFinish={saveSaml}>
            <Form.Item name="enabled" label="Bật SAML SSO" valuePropName="checked">
              <Switch disabled={saml?.enterpriseRequired} />
            </Form.Item>
            <Form.Item name="idpName" label="Tên IdP (Okta, Azure AD, ...)">
              <Input placeholder="Azure Active Directory" disabled={saml?.enterpriseRequired} />
            </Form.Item>
            <Form.Item name="entityId" label="Entity ID (IdP)">
              <Input disabled={saml?.enterpriseRequired} />
            </Form.Item>
            <Form.Item name="ssoUrl" label="SSO URL">
              <Input placeholder="https://login.microsoftonline.com/..." disabled={saml?.enterpriseRequired} />
            </Form.Item>
            <Form.Item name="certificate" label="Certificate (X.509)">
              <Input.TextArea rows={4} disabled={saml?.enterpriseRequired} />
            </Form.Item>
            <Form.Item name="attributeEmail" label="Attribute email">
              <Input disabled={saml?.enterpriseRequired} />
            </Form.Item>
            {saml?.metadataUrl && (
              <Paragraph type="secondary">
                Metadata SP: <Text code>{saml.metadataUrl}</Text>
                <Button type="link" size="small" icon={<Copy size={12} />} onClick={() => copyText(saml.metadataUrl)} />
              </Paragraph>
            )}
            <Button type="primary" htmlType="submit" disabled={saml?.enterpriseRequired}>Lưu cấu hình SAML</Button>
          </Form>
        </Card>
      ),
    },
  ];

  return (
    <div>
      <PageHeader title="Tích hợp" subtitle="API Keys, Webhooks và SAML SSO doanh nghiệp" />
      <Tabs items={tabItems} />

      <Modal title="Tạo API Key" open={keyModal} onCancel={() => setKeyModal(false)} footer={null} destroyOnClose>
        <Form form={keyForm} layout="vertical" onFinish={createApiKey}>
          <Form.Item name="name" label="Tên" rules={[{ required: true, message: 'Nhập tên key' }]}>
            <Input placeholder="Production Integration" />
          </Form.Item>
          <Form.Item name="scopes" label="Quyền" initialValue="employees:read,departments:read">
            <Input />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>Tạo key</Button>
        </Form>
      </Modal>

      <Modal
        title="API Key mới — sao chép ngay"
        open={!!newRawKey}
        onCancel={() => setNewRawKey(null)}
        footer={<Button onClick={() => setNewRawKey(null)}>Đã lưu</Button>}
      >
        <Alert type="warning" showIcon message="Key chỉ hiển thị một lần. Hãy lưu an toàn." style={{ marginBottom: 12 }} />
        <Input.TextArea value={newRawKey} readOnly rows={3} />
        <Button icon={<Copy size={16} />} onClick={() => copyText(newRawKey)} style={{ marginTop: 8 }}>Sao chép</Button>
      </Modal>

      <Modal title="Thêm Webhook" open={webhookModal} onCancel={() => setWebhookModal(false)} footer={null} destroyOnClose>
        <Form form={webhookForm} layout="vertical" onFinish={createWebhook}>
          <Form.Item name="name" label="Tên" rules={[{ required: true }]}>
            <Input placeholder="Slack / Zapier" />
          </Form.Item>
          <Form.Item name="url" label="Endpoint URL" rules={[{ required: true, type: 'url' }]}>
            <Input placeholder="https://hooks.example.com/..." />
          </Form.Item>
          <Form.Item name="events" label="Sự kiện" rules={[{ required: true }]}>
            <Select mode="multiple" options={WEBHOOK_EVENTS} />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>Thêm</Button>
        </Form>
      </Modal>
    </div>
  );
};

export default IntegrationsPage;
