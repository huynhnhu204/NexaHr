import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, InputNumber, Tag, message, Space, Drawer, DatePicker } from 'antd';
import { Plus } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { PERFORMANCE_RATINGS, PERFORMANCE_REVIEW_STATUS } from '../../utils/constants';
import { usePermission } from '../../hooks/usePermission';
import { useAuth } from '../../hooks/useAuth';
import { formatDate, formatDateTime } from '../../utils/formatDate';

const PerformancePage = () => {
  const { hasRole } = usePermission();
  const { user } = useAuth();
  const [reviews, setReviews] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [selected, setSelected] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [selfComment, setSelfComment] = useState('');
  const [form] = Form.useForm();

  const canManage = hasRole('ADMIN', 'HR', 'MANAGER');
  const employeeId = user?.employeeId;

  const fetch = async () => {
    setLoading(true);
    try {
      const endpoint = canManage ? ENDPOINTS.PERFORMANCE.BASE : ENDPOINTS.PERFORMANCE.MY;
      const res = await axiosClient.get(endpoint, { params: { size: 50 } });
      setReviews(res.data?.content || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetch();
    if (canManage) {
      axiosClient.get(ENDPOINTS.EMPLOYEES.BASE, { params: { size: 100 } }).then((r) => setEmployees(r.data?.content || []));
    }
  }, []);

  const handleSubmit = async (values) => {
    const payload = {
      ...values,
      dueDate: values.dueDate ? values.dueDate.format('YYYY-MM-DD') : null,
    };
    await axiosClient.post(ENDPOINTS.PERFORMANCE.BASE, payload);
    message.success('Đã tạo đánh giá hiệu suất');
    setModalOpen(false);
    form.resetFields();
    fetch();
  };

  const publish = async (id) => {
    await axiosClient.put(ENDPOINTS.PERFORMANCE.PUBLISH(id));
    message.success('Đã gửi cho nhân viên tự đánh giá');
    fetch();
    if (selected?.id === id) setSelected((s) => ({ ...s, status: 'PENDING_SELF' }));
  };

  const submitSelf = async () => {
    await axiosClient.put(ENDPOINTS.PERFORMANCE.SELF_REVIEW(selected.id), { employeeSelfComment: selfComment });
    message.success('Đã gửi tự đánh giá');
    setDrawerOpen(false);
    fetch();
  };

  const finalize = async (values) => {
    await axiosClient.put(ENDPOINTS.PERFORMANCE.FINALIZE(selected.id), values);
    message.success('Đã hoàn tất đánh giá');
    setDrawerOpen(false);
    fetch();
  };

  const columns = [
    { title: 'Nhân viên', dataIndex: 'employeeName' },
    { title: 'Kỳ', dataIndex: 'reviewPeriod' },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      render: (s) => {
        const meta = PERFORMANCE_REVIEW_STATUS[s] || PERFORMANCE_REVIEW_STATUS.DRAFT;
        return <Tag color={meta.color}>{meta.label}</Tag>;
      },
    },
    { title: 'Điểm', dataIndex: 'score' },
    { title: 'Xếp loại', dataIndex: 'rating', render: (r) => r ? <Tag color={PERFORMANCE_RATINGS[r]?.color}>{PERFORMANCE_RATINGS[r]?.label}</Tag> : '-' },
    { title: 'Hạn', dataIndex: 'dueDate', render: (v) => v ? formatDate(v) : '—' },
    {
      title: '',
      render: (_, record) => (
        <Button size="small" onClick={() => { setSelected(record); setSelfComment(record.employeeSelfComment || ''); setDrawerOpen(true); }}>
          Chi tiết
        </Button>
      ),
    },
  ];

  return (
    <div>
      <div className="page-header">
        <div><h2>Đánh giá hiệu suất</h2><p>Quy trình: Tạo → Tự đánh giá → Quản lý chấm điểm</p></div>
        {canManage && (
          <Button type="primary" icon={<Plus size={16} />} onClick={() => setModalOpen(true)} style={{ background: '#1e3a5f' }}>
            Tạo đánh giá mới
          </Button>
        )}
      </div>
      <div className="data-table-card">
        <Table columns={columns} dataSource={reviews} rowKey="id" loading={loading} pagination={{ pageSize: 10 }} />
      </div>

      <Modal title="Tạo đánh giá hiệu suất" open={modalOpen} onCancel={() => setModalOpen(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="employeeId" label="Nhân viên" rules={[{ required: true }]}>
            <Select options={employees.map((e) => ({ value: e.id, label: e.fullName }))} />
          </Form.Item>
          <Form.Item name="reviewPeriod" label="Kỳ đánh giá" rules={[{ required: true }]}>
            <Input placeholder="Q1 2026" />
          </Form.Item>
          <Form.Item name="goals" label="Mục tiêu / KPI"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="dueDate" label="Hạn hoàn thành"><DatePicker style={{ width: '100%' }} /></Form.Item>
          <Button type="primary" htmlType="submit" block style={{ background: '#1e3a5f' }}>Tạo nháp</Button>
        </Form>
      </Modal>

      <Drawer title="Chi tiết đánh giá" open={drawerOpen} onClose={() => setDrawerOpen(false)} width={480}>
        {selected && (
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <div><Tag>{PERFORMANCE_REVIEW_STATUS[selected.status]?.label}</Tag></div>
            <div><strong>Nhân viên:</strong> {selected.employeeName}</div>
            <div><strong>Kỳ:</strong> {selected.reviewPeriod}</div>
            {selected.goals && <div><strong>Mục tiêu:</strong><br />{selected.goals}</div>}
            {selected.employeeSelfComment && <div><strong>Tự đánh giá:</strong><br />{selected.employeeSelfComment}</div>}

            {canManage && selected.status === 'DRAFT' && (
              <Button type="primary" block onClick={() => publish(selected.id)}>Gửi cho nhân viên</Button>
            )}

            {selected.status === 'PENDING_SELF' && selected.employeeId === employeeId && (
              <>
                <Input.TextArea rows={4} value={selfComment} onChange={(e) => setSelfComment(e.target.value)} placeholder="Tự đánh giá của bạn..." />
                <Button type="primary" block onClick={submitSelf}>Gửi tự đánh giá</Button>
              </>
            )}

            {canManage && selected.status === 'PENDING_MANAGER' && (
              <Form layout="vertical" onFinish={finalize} initialValues={selected}>
                <Form.Item name="score" label="Điểm (0-100)"><InputNumber min={0} max={100} style={{ width: '100%' }} /></Form.Item>
                <Form.Item name="rating" label="Xếp loại">
                  <Select options={Object.entries(PERFORMANCE_RATINGS).map(([k, v]) => ({ value: k, label: v.label }))} />
                </Form.Item>
                <Form.Item name="comment" label="Nhận xét quản lý"><Input.TextArea rows={3} /></Form.Item>
                <Button type="primary" htmlType="submit" block>Hoàn tất đánh giá</Button>
              </Form>
            )}

            {selected.status === 'COMPLETED' && (
              <div>
                <div><strong>Điểm:</strong> {selected.score}</div>
                <div><strong>Nhận xét:</strong> {selected.comment}</div>
                <div style={{ color: '#94A3B8', fontSize: 12 }}>Hoàn tất: {formatDateTime(selected.createdAt)}</div>
              </div>
            )}
          </Space>
        )}
      </Drawer>
    </div>
  );
};

export default PerformancePage;
