import { useEffect, useState } from 'react';
import { Card, Table, Button, Form, Input, Modal, message, Tag, Select, Switch, Space } from 'antd';
import { Plus, Trash2, Users } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { ROLE_LABELS } from '../../utils/constants';
import { formatDateTime } from '../../utils/formatDate';

const BASE_ROLES = ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'];

const CustomRolesPage = () => {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const fetchRoles = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.CUSTOM_ROLES.BASE);
      setRoles(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchRoles(); }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ active: true, baseRole: 'EMPLOYEE' });
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const save = async (values) => {
    if (editing) {
      await axiosClient.put(ENDPOINTS.CUSTOM_ROLES.ROLE(editing.id), values);
      message.success('Cập nhật vai trò thành công');
    } else {
      await axiosClient.post(ENDPOINTS.CUSTOM_ROLES.BASE, values);
      message.success('Tạo vai trò thành công');
    }
    setModalOpen(false);
    fetchRoles();
  };

  const remove = (id) => {
    Modal.confirm({
      title: 'Xóa vai trò tùy chỉnh?',
      okType: 'danger',
      onOk: async () => {
        await axiosClient.delete(ENDPOINTS.CUSTOM_ROLES.ROLE(id));
        message.success('Đã xóa vai trò');
        fetchRoles();
      },
    });
  };

  const columns = [
    { title: 'Tên', dataIndex: 'name' },
    { title: 'Mã', dataIndex: 'code', render: (v) => <Tag>{v}</Tag> },
    { title: 'Vai trò gốc', dataIndex: 'baseRole', render: (v) => ROLE_LABELS[v] || v },
    { title: 'Người dùng', dataIndex: 'assignedUsers', render: (v) => <><Users size={14} /> {v}</> },
    { title: 'Trạng thái', dataIndex: 'active', render: (v) => <Tag color={v ? 'green' : 'default'}>{v ? 'Hoạt động' : 'Tắt'}</Tag> },
    { title: 'Tạo lúc', dataIndex: 'createdAt', render: (v) => formatDateTime(v) },
    {
      title: '',
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => openEdit(record)}>Sửa</Button>
          <Button size="small" danger icon={<Trash2 size={14} />} onClick={() => remove(record.id)} />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Vai trò tùy chỉnh"
        subtitle="Tạo vai trò riêng với bộ quyền linh hoạt — gán cho nhân viên trong hồ sơ"
        extra={<Button type="primary" icon={<Plus size={16} />} onClick={openCreate}>Thêm vai trò</Button>}
      />

      <Card>
        <Table rowKey="id" loading={loading} dataSource={roles} columns={columns} pagination={false} />
      </Card>

      <Modal title={editing ? 'Sửa vai trò' : 'Thêm vai trò'} open={modalOpen} onCancel={() => setModalOpen(false)} onOk={() => form.submit()} okText="Lưu">
        <Form form={form} layout="vertical" onFinish={save}>
          <Form.Item name="name" label="Tên vai trò" rules={[{ required: true }]}>
            <Input placeholder="VD: Chuyên viên Payroll" />
          </Form.Item>
          <Form.Item name="code" label="Mã" rules={[{ required: true }]}>
            <Input placeholder="PAYROLL_SPEC" disabled={!!editing} />
          </Form.Item>
          <Form.Item name="baseRole" label="Vai trò gốc" rules={[{ required: true }]}>
            <Select options={BASE_ROLES.map((r) => ({ value: r, label: ROLE_LABELS[r] }))} />
          </Form.Item>
          <Form.Item name="description" label="Mô tả"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="active" label="Kích hoạt" valuePropName="checked"><Switch /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CustomRolesPage;
