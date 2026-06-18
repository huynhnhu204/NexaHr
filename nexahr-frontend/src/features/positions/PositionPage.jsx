import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, message, Popconfirm } from 'antd';
import { Plus, Edit, Trash2 } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { formatCurrency } from '../../utils/formatCurrency';
import { usePermission } from '../../hooks/usePermission';
import PageHeader from '../../components/common/PageHeader';

const PositionPage = () => {
  const { isAdmin } = usePermission();
  const [positions, setPositions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const loadPositions = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.POSITIONS);
      const list = Array.isArray(res?.data) ? res.data : [];
      setPositions(list);
    } catch (err) {
      message.error(err.message || 'Không tải được danh sách chức vụ');
      setPositions([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadPositions(); }, []);

  const handleSubmit = async (values) => {
    try {
      if (editing) {
        await axiosClient.put(`${ENDPOINTS.POSITIONS}/${editing.id}`, values);
        message.success('Cập nhật chức vụ thành công');
      } else {
        await axiosClient.post(ENDPOINTS.POSITIONS, values);
        message.success('Thêm chức vụ thành công');
      }
      setModalOpen(false);
      form.resetFields();
      setEditing(null);
      loadPositions();
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleDelete = async (id) => {
    try {
      await axiosClient.delete(`${ENDPOINTS.POSITIONS}/${id}`);
      message.success('Đã xóa chức vụ');
      loadPositions();
    } catch (err) {
      message.error(err.message || 'Không thể xóa chức vụ');
    }
  };

  const columns = [
    { title: 'Chức vụ', dataIndex: 'name' },
    { title: 'Lương cơ bản', dataIndex: 'baseSalary', render: (v) => formatCurrency(v) },
    { title: 'Mô tả', dataIndex: 'description', render: (v) => v || '-' },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, r) => (
        <>
          <Button
            type="text"
            icon={<Edit size={16} />}
            onClick={() => {
              setEditing(r);
              form.setFieldsValue({
                name: r.name,
                baseSalary: r.baseSalary,
                description: r.description,
              });
              setModalOpen(true);
            }}
          />
          {isAdmin && (
            <Popconfirm title="Xóa chức vụ này?" okText="Xóa" cancelText="Hủy" onConfirm={() => handleDelete(r.id)}>
              <Button type="text" danger icon={<Trash2 size={16} />} />
            </Popconfirm>
          )}
        </>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Chức vụ"
        subtitle="Quản lý chức vụ và bậc lương"
        extra={(
          <Button
            type="primary"
            icon={<Plus size={16} />}
            onClick={() => { setEditing(null); form.resetFields(); setModalOpen(true); }}
            style={{ background: '#1E3A8A' }}
          >
            Thêm chức vụ
          </Button>
        )}
      />

      <div className="data-table-card">
        <Table
          columns={columns}
          dataSource={positions}
          rowKey="id"
          loading={loading}
          pagination={false}
          locale={{ emptyText: 'Chưa có chức vụ. Chọn công ty "NexaHR Demo" hoặc thêm chức vụ mới.' }}
        />
      </div>

      <Modal title={editing ? 'Sửa chức vụ' : 'Thêm chức vụ'} open={modalOpen} onCancel={() => setModalOpen(false)} footer={null} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="Tên chức vụ" rules={[{ required: true, message: 'Nhập tên chức vụ' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="baseSalary" label="Lương cơ bản">
            <InputNumber
              style={{ width: '100%' }}
              min={0}
              formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(v) => v?.replace(/,/g, '')}
            />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Button type="primary" htmlType="submit" block style={{ background: '#1E3A8A' }}>Lưu</Button>
        </Form>
      </Modal>
    </div>
  );
};

export default PositionPage;
