import { useEffect, useState } from 'react';
import { Table, Button, Tag, Modal, Form, Select, DatePicker, Input, message, Space } from 'antd';
import { Plus, Check, X } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { LEAVE_STATUS, LEAVE_TYPES } from '../../utils/constants';
import { formatDate } from '../../utils/formatDate';
import { usePermission } from '../../hooks/usePermission';

const LeavePage = () => {
  const { canApproveLeave } = usePermission();
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [rejectModal, setRejectModal] = useState(null);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [form] = Form.useForm();
  const [rejectForm] = Form.useForm();

  const fetchLeaves = async () => {
    setLoading(true);
    try {
      const endpoint = canApproveLeave ? ENDPOINTS.LEAVES.BASE : ENDPOINTS.LEAVES.MY;
      const res = await axiosClient.get(endpoint, { params: { page, size: 10 } });
      setLeaves(res.data?.content || []);
      setTotal(res.data?.totalElements || 0);
    } catch (err) {
      message.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchLeaves(); }, [page]);

  const handleSubmit = async (values) => {
    try {
      await axiosClient.post(ENDPOINTS.LEAVES.BASE, {
        ...values,
        startDate: values.dates[0].format('YYYY-MM-DD'),
        endDate: values.dates[1].format('YYYY-MM-DD'),
      });
      message.success('Đã gửi đơn nghỉ phép');
      setModalOpen(false);
      form.resetFields();
      fetchLeaves();
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleApprove = async (id) => {
    try {
      await axiosClient.put(ENDPOINTS.LEAVES.APPROVE(id));
      message.success('Đã duyệt đơn nghỉ phép');
      fetchLeaves();
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleReject = async (values) => {
    try {
      await axiosClient.put(ENDPOINTS.LEAVES.REJECT(rejectModal), { reason: values.reason });
      message.success('Đã từ chối đơn nghỉ phép');
      setRejectModal(null);
      rejectForm.resetFields();
      fetchLeaves();
    } catch (err) {
      message.error(err.message);
    }
  };

  const columns = [
    ...(canApproveLeave ? [{ title: 'Nhân viên', dataIndex: 'employeeName' }] : []),
    { title: 'Loại', dataIndex: 'leaveType', render: (t) => LEAVE_TYPES[t] || t },
    { title: 'Từ ngày', dataIndex: 'startDate', render: (v) => formatDate(v) },
    { title: 'Đến ngày', dataIndex: 'endDate', render: (v) => formatDate(v) },
    { title: 'Số ngày', dataIndex: 'totalDays' },
    {
      title: 'Trạng thái', dataIndex: 'status',
      render: (s) => <Tag color={LEAVE_STATUS[s]?.color}>{LEAVE_STATUS[s]?.label}</Tag>,
    },
    {
      title: 'Thao tác', key: 'actions',
      render: (_, r) => r.status === 'PENDING' && canApproveLeave ? (
        <Space>
          <Button size="small" type="primary" icon={<Check size={14} />} onClick={() => handleApprove(r.id)} style={{ background: '#10b981' }}>Duyệt</Button>
          <Button size="small" danger icon={<X size={14} />} onClick={() => setRejectModal(r.id)}>Từ chối</Button>
        </Space>
      ) : null,
    },
  ];

  return (
    <div>
      <div className="page-header">
        <div><h2>Quản lý nghỉ phép</h2><p>Tạo và quản lý đơn nghỉ phép</p></div>
        <Button type="primary" icon={<Plus size={16} />} onClick={() => setModalOpen(true)} style={{ background: '#1e3a5f' }}>
          Tạo đơn nghỉ phép
        </Button>
      </div>

      <div className="data-table-card">
        <Table columns={columns} dataSource={leaves} rowKey="id" loading={loading}
          locale={{ emptyText: 'Chưa có đơn nghỉ phép. Nhấn "Tạo đơn nghỉ phép" để bắt đầu.' }}
          pagination={{ current: page + 1, total, pageSize: 10, onChange: (p) => setPage(p - 1) }} />
      </div>

      <Modal title="Tạo đơn nghỉ phép" open={modalOpen} onCancel={() => setModalOpen(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="leaveType" label="Loại nghỉ phép" rules={[{ required: true }]}>
            <Select options={Object.entries(LEAVE_TYPES).map(([k, v]) => ({ value: k, label: v }))} />
          </Form.Item>
          <Form.Item name="dates" label="Khoảng thời gian" rules={[{ required: true }]}>
            <DatePicker.RangePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="reason" label="Lý do">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Button type="primary" htmlType="submit" block style={{ background: '#1e3a5f' }}>Gửi đơn</Button>
        </Form>
      </Modal>

      <Modal title="Từ chối đơn nghỉ phép" open={!!rejectModal} onCancel={() => setRejectModal(null)} footer={null}>
        <Form form={rejectForm} layout="vertical" onFinish={handleReject}>
          <Form.Item name="reason" label="Lý do từ chối" rules={[{ required: true }]}>
            <Input.TextArea rows={3} />
          </Form.Item>
          <Button danger htmlType="submit" block>Xác nhận từ chối</Button>
        </Form>
      </Modal>
    </div>
  );
};

export default LeavePage;
