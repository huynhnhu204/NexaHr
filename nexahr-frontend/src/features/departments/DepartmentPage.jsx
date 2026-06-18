import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Popconfirm } from 'antd';
import { Plus, Edit, Trash2 } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';

const DepartmentPage = () => {
  const [departments, setDepartments] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const fetch = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.DEPARTMENTS);
      setDepartments(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetch();
    axiosClient.get(ENDPOINTS.EMPLOYEES.BASE, { params: { size: 100 } }).then((r) => setEmployees(r.data?.content || []));
  }, []);

  const handleSubmit = async (values) => {
    try {
      if (editing) {
        await axiosClient.put(`${ENDPOINTS.DEPARTMENTS}/${editing.id}`, values);
        message.success('Cập nhật phòng ban thành công');
      } else {
        await axiosClient.post(ENDPOINTS.DEPARTMENTS, values);
        message.success('Thêm phòng ban thành công');
      }
      setModalOpen(false);
      form.resetFields();
      setEditing(null);
      fetch();
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleDelete = async (id) => {
    try {
      await axiosClient.delete(`${ENDPOINTS.DEPARTMENTS}/${id}`);
      message.success('Xóa phòng ban thành công');
      fetch();
    } catch (err) {
      message.error(err.message);
    }
  };

  const columns = [
    { title: 'Tên phòng ban', dataIndex: 'name' },
    { title: 'Mô tả', dataIndex: 'description', render: (v) => v || '-' },
    { title: 'Trưởng phòng', dataIndex: 'managerName', render: (v) => v || '-' },
    { title: 'Số nhân viên', dataIndex: 'employeeCount' },
    {
      title: 'Thao tác', key: 'actions',
      render: (_, r) => (
        <>
          <Button type="text" icon={<Edit size={16} />} onClick={() => { setEditing(r); form.setFieldsValue(r); setModalOpen(true); }} />
          <Popconfirm title="Xóa phòng ban này?" okText="Xóa" cancelText="Hủy" onConfirm={() => handleDelete(r.id)}>
            <Button type="text" danger icon={<Trash2 size={16} />} />
          </Popconfirm>
        </>
      ),
    },
  ];

  return (
    <div>
      <div className="page-header">
        <div><h2>Phòng ban</h2><p>Quản lý cơ cấu tổ chức công ty</p></div>
        <Button type="primary" icon={<Plus size={16} />} onClick={() => { setEditing(null); form.resetFields(); setModalOpen(true); }}
          style={{ background: '#1e3a5f' }}>Thêm phòng ban</Button>
      </div>
      <div className="data-table-card">
        <Table columns={columns} dataSource={departments} rowKey="id" loading={loading} pagination={false} />
      </div>
      <Modal title={editing ? 'Sửa phòng ban' : 'Thêm phòng ban'} open={modalOpen} onCancel={() => setModalOpen(false)} footer={null} okText="Lưu" cancelText="Hủy">
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="Tên phòng ban" rules={[{ required: true, message: 'Vui lòng nhập tên phòng ban' }]}><Input /></Form.Item>
          <Form.Item name="description" label="Mô tả"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="managerId" label="Trưởng phòng">
            <Select allowClear placeholder="Chọn trưởng phòng" options={employees.map((e) => ({ value: e.id, label: e.fullName }))} />
          </Form.Item>
          <Button type="primary" htmlType="submit" block style={{ background: '#1e3a5f' }}>Lưu</Button>
        </Form>
      </Modal>
    </div>
  );
};

export default DepartmentPage;
