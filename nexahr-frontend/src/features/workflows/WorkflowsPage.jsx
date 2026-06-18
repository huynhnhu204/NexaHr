import { useEffect, useState } from 'react';
import {
  Card, Table, Button, Form, Input, Modal, message, Tag, Select, Switch, Space, Alert,
} from 'antd';
import { GitBranch, Plus, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { formatDateTime } from '../../utils/formatDate';

const TRIGGERS = [
  { value: 'LEAVE_CREATED', label: 'Khi tạo đơn nghỉ phép' },
  { value: 'PAYROLL_GENERATED', label: 'Khi tạo bảng lương' },
  { value: 'EMPLOYEE_CREATED', label: 'Khi thêm nhân viên mới' },
];

const ACTIONS = [
  { value: 'NOTIFY_MANAGER', label: 'Thông báo quản lý trực tiếp' },
  { value: 'NOTIFY_HR', label: 'Thông báo phòng HR' },
  { value: 'AUTO_APPROVE_LEAVE_DAYS_LTE', label: 'Tự động duyệt nghỉ phép (≤ N ngày)' },
  { value: 'SEND_EMAIL', label: 'Gửi email cho quản lý' },
];

const TRIGGER_LABEL = Object.fromEntries(TRIGGERS.map((t) => [t.value, t.label]));
const ACTION_LABEL = Object.fromEntries(ACTIONS.map((a) => [a.value, a.label]));

const WorkflowsPage = () => {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const fetchRules = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.WORKFLOWS.BASE);
      setRules(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchRules(); }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ active: true });
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const saveRule = async (values) => {
    if (editing) {
      await axiosClient.put(ENDPOINTS.WORKFLOWS.RULE(editing.id), values);
      message.success('Cập nhật quy trình thành công');
    } else {
      await axiosClient.post(ENDPOINTS.WORKFLOWS.BASE, values);
      message.success('Tạo quy trình thành công');
    }
    setModalOpen(false);
    fetchRules();
  };

  const deleteRule = (id) => {
    Modal.confirm({
      title: 'Xóa quy trình tự động?',
      okText: 'Xóa',
      okType: 'danger',
      onOk: async () => {
        await axiosClient.delete(ENDPOINTS.WORKFLOWS.RULE(id));
        message.success('Đã xóa quy trình');
        fetchRules();
      },
    });
  };

  const columns = [
    { title: 'Tên quy trình', dataIndex: 'name', key: 'name' },
    { title: 'Kích hoạt khi', dataIndex: 'trigger', key: 'trigger', render: (v) => TRIGGER_LABEL[v] || v },
    { title: 'Hành động', dataIndex: 'action', key: 'action', render: (v) => ACTION_LABEL[v] || v },
    { title: 'Cấu hình', dataIndex: 'configValue', key: 'configValue', render: (v) => v || '—' },
    { title: 'Trạng thái', dataIndex: 'active', key: 'active', render: (v) => <Tag color={v ? 'green' : 'default'}>{v ? 'Bật' : 'Tắt'}</Tag> },
    { title: 'Tạo lúc', dataIndex: 'createdAt', key: 'createdAt', render: (v) => formatDateTime(v) },
    {
      title: '',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => openEdit(record)}>Sửa</Button>
          <Button size="small" danger icon={<Trash2 size={14} />} onClick={() => deleteRule(record.id)} />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Quy trình tự động"
        subtitle="Tự động hóa duyệt nghỉ phép, thông báo và email"
        extra={(
          <Button type="primary" icon={<Plus size={16} />} onClick={openCreate}>
            Thêm quy trình
          </Button>
        )}
      />

      <Alert
        type="info"
        showIcon
        icon={<GitBranch size={16} />}
        message="Workflow automation"
        description="Quy trình chạy tự động khi nhân viên tạo đơn nghỉ phép. Demo: tự động duyệt đơn ≤ 2 ngày và thông báo quản lý."
        style={{ marginBottom: 16 }}
      />

      <Card>
        <Table rowKey="id" loading={loading} dataSource={rules} columns={columns} pagination={false} />
      </Card>

      <Modal
        title={editing ? 'Sửa quy trình' : 'Thêm quy trình'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText="Lưu"
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={saveRule}>
          <Form.Item name="name" label="Tên quy trình" rules={[{ required: true, message: 'Nhập tên quy trình' }]}>
            <Input placeholder="VD: Thông báo HR khi có đơn nghỉ phép" />
          </Form.Item>
          <Form.Item name="trigger" label="Kích hoạt khi" rules={[{ required: true }]}>
            <Select options={TRIGGERS} />
          </Form.Item>
          <Form.Item name="action" label="Hành động" rules={[{ required: true }]}>
            <Select options={ACTIONS} />
          </Form.Item>
          <Form.Item
            noStyle
            shouldUpdate={(prev, cur) => prev.action !== cur.action}
          >
            {({ getFieldValue }) => getFieldValue('action') === 'AUTO_APPROVE_LEAVE_DAYS_LTE' && (
              <Form.Item name="configValue" label="Số ngày tối đa" rules={[{ required: true, message: 'Nhập số ngày' }]}>
                <Input placeholder="VD: 2" />
              </Form.Item>
            )}
          </Form.Item>
          <Form.Item name="active" label="Kích hoạt" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default WorkflowsPage;
