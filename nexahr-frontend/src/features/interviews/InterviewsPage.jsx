import { useEffect, useState } from 'react';
import {
  Table, Button, Select, Drawer, Form, Input, DatePicker, InputNumber,
  Space, message, Popconfirm, Modal,
} from 'antd';
import { Plus, Edit, Trash2, CheckCircle } from 'lucide-react';
import dayjs from 'dayjs';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { INTERVIEW_STATUS, INTERVIEW_MODE } from '../../utils/constants';
import { formatDateTime } from '../../utils/formatDate';
import useInterviews from './hooks/useInterviews';

const InterviewsPage = () => {
  const {
    interviews, loading, total, page, setPage, filters, setFilters,
    createInterview, updateInterview, deleteInterview, completeInterview,
  } = useInterviews();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [completeOpen, setCompleteOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [completing, setCompleting] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [form] = Form.useForm();
  const [completeForm] = Form.useForm();

  useEffect(() => {
    Promise.all([
      axiosClient.get(ENDPOINTS.CANDIDATES, { params: { size: 100 } }),
      axiosClient.get(ENDPOINTS.EMPLOYEES.BASE, { params: { size: 100 } }),
    ]).then(([cRes, eRes]) => {
      setCandidates(cRes.data?.content || []);
      setEmployees(eRes.data?.content || []);
    });
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setDrawerOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue({
      ...record,
      scheduledAt: record.scheduledAt ? dayjs(record.scheduledAt) : null,
    });
    setDrawerOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload = {
      ...values,
      scheduledAt: values.scheduledAt?.format('YYYY-MM-DDTHH:mm:ss'),
      status: values.status || 'SCHEDULED',
    };
    try {
      if (editing) await updateInterview(editing.id, payload);
      else await createInterview(payload);
      message.success(editing ? 'Cập nhật thành công' : 'Tạo lịch phỏng vấn thành công');
      setDrawerOpen(false);
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleComplete = async () => {
    const values = await completeForm.validateFields();
    try {
      await completeInterview(completing.id, { evaluation: values.evaluation, notes: values.notes, status: 'COMPLETED' });
      message.success('Hoàn thành đánh giá phỏng vấn');
      setCompleteOpen(false);
    } catch (err) {
      message.error(err.message);
    }
  };

  const columns = [
    { title: 'Ứng viên', dataIndex: 'candidateName' },
    { title: 'Người PV', dataIndex: 'interviewerName' },
    { title: 'Thời gian', dataIndex: 'scheduledAt', render: (v) => formatDateTime(v) },
    { title: 'Hình thức', dataIndex: 'mode', render: (m) => <StatusBadge status={m} map={INTERVIEW_MODE} /> },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => <StatusBadge status={s} map={INTERVIEW_STATUS} /> },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button type="text" icon={<Edit size={16} />} onClick={() => openEdit(record)} />
          {record.status === 'SCHEDULED' && (
            <Button type="text" icon={<CheckCircle size={16} />} onClick={() => { setCompleting(record); completeForm.resetFields(); setCompleteOpen(true); }} />
          )}
          <Popconfirm title="Xóa lịch phỏng vấn?" onConfirm={() => deleteInterview(record.id)} okText="Xóa" cancelText="Hủy">
            <Button type="text" danger icon={<Trash2 size={16} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Lịch phỏng vấn"
        subtitle="Lên lịch, phân công người phỏng vấn và đánh giá ứng viên"
        extra={<Button type="primary" icon={<Plus size={16} />} onClick={openCreate} style={{ background: '#1E3A8A' }}>Tạo lịch phỏng vấn</Button>}
      />

      <div className="filter-bar">
        <Select placeholder="Trạng thái" allowClear style={{ width: 160 }}
          onChange={(v) => { setFilters({ ...filters, status: v }); setPage(0); }}
          options={Object.entries(INTERVIEW_STATUS).map(([k, v]) => ({ value: k, label: v.label }))} />
      </div>

      <div className="data-table-card table-responsive">
        <Table
          columns={columns}
          dataSource={interviews}
          rowKey="id"
          loading={loading}
          pagination={{ current: page + 1, total, pageSize: 10, onChange: (p) => setPage(p - 1) }}
          locale={{ emptyText: <EmptyState title="Chưa có lịch phỏng vấn" /> }}
        />
      </div>

      <Drawer
        title={editing ? 'Sửa lịch phỏng vấn' : 'Tạo lịch phỏng vấn'}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        width={480}
        extra={<Button type="primary" onClick={handleSubmit} style={{ background: '#1E3A8A' }}>Lưu</Button>}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="candidateId" label="Ứng viên" rules={[{ required: true, message: 'Chọn ứng viên' }]}>
            <Select showSearch optionFilterProp="label"
              options={candidates.map((c) => ({ value: c.id, label: c.fullName }))} />
          </Form.Item>
          <Form.Item name="interviewerId" label="Người phỏng vấn" rules={[{ required: true, message: 'Chọn người phỏng vấn' }]}>
            <Select showSearch optionFilterProp="label"
              options={employees.map((e) => ({ value: e.id, label: e.fullName }))} />
          </Form.Item>
          <Form.Item name="scheduledAt" label="Thời gian" rules={[{ required: true, message: 'Chọn thời gian' }]}>
            <DatePicker showTime style={{ width: '100%' }} format="DD/MM/YYYY HH:mm" />
          </Form.Item>
          <Form.Item name="duration" label="Thời lượng (phút)">
            <InputNumber min={15} max={180} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="mode" label="Hình thức" initialValue="OFFLINE">
            <Select options={Object.entries(INTERVIEW_MODE).map(([k, v]) => ({ value: k, label: v.label }))} />
          </Form.Item>
          <Form.Item name="location" label="Địa điểm">
            <Input placeholder="Phòng họp A" />
          </Form.Item>
          <Form.Item name="meetingLink" label="Link họp">
            <Input placeholder="https://meet.google.com/..." />
          </Form.Item>
          <Form.Item name="notes" label="Ghi chú">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Drawer>

      <Modal title="Hoàn thành đánh giá" open={completeOpen} onCancel={() => setCompleteOpen(false)} onOk={handleComplete} okText="Hoàn thành">
        <Form form={completeForm} layout="vertical">
          <Form.Item name="evaluation" label="Đánh giá" rules={[{ required: true, message: 'Nhập đánh giá' }]}>
            <Input.TextArea rows={4} placeholder="Nhận xét về ứng viên..." />
          </Form.Item>
          <Form.Item name="notes" label="Ghi chú thêm">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default InterviewsPage;
