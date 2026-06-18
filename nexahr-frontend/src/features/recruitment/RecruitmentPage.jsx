import { useEffect, useState } from 'react';
import { Table, Button, Tabs, Tag, Modal, Form, Input, Select, message, Space, Switch } from 'antd';
import { Plus } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { CANDIDATE_STATUS, JOB_STATUS } from '../../utils/constants';
import { formatDate } from '../../utils/formatDate';

const RecruitmentPage = () => {
  const [jobs, setJobs] = useState([]);
  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [jobModal, setJobModal] = useState(false);
  const [candidateModal, setCandidateModal] = useState(false);
  const [departments, setDepartments] = useState([]);
  const [positions, setPositions] = useState([]);
  const [jobForm] = Form.useForm();
  const [candidateForm] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const [jobRes, candRes] = await Promise.all([
        axiosClient.get(ENDPOINTS.JOBS, { params: { size: 50 } }),
        axiosClient.get(ENDPOINTS.CANDIDATES, { params: { size: 100 } }),
      ]);
      setJobs(jobRes.data?.content || []);
      setCandidates(candRes.data?.content || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    axiosClient.get(ENDPOINTS.DEPARTMENTS).then((r) => setDepartments(r.data || []));
    axiosClient.get(ENDPOINTS.POSITIONS).then((r) => setPositions(r.data || []));
  }, []);

  const handleCreateJob = async (values) => {
    try {
      await axiosClient.post(ENDPOINTS.JOBS, values);
      message.success('Đã tạo tin tuyển dụng');
      setJobModal(false);
      jobForm.resetFields();
      fetchData();
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleCreateCandidate = async (values) => {
    try {
      await axiosClient.post(ENDPOINTS.CANDIDATES, values);
      message.success('Đã thêm ứng viên');
      setCandidateModal(false);
      candidateForm.resetFields();
      fetchData();
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleStatusChange = async (id, status) => {
    try {
      await axiosClient.put(`/candidates/${id}/status`, { status });
      message.success('Đã cập nhật trạng thái');
      fetchData();
    } catch (err) {
      message.error(err.message);
    }
  };

  const jobColumns = [
    { title: 'Tiêu đề', dataIndex: 'title' },
    { title: 'Phòng ban', dataIndex: 'departmentName' },
    { title: 'Chức vụ', dataIndex: 'positionName' },
    { title: 'Mức lương', dataIndex: 'salaryRange' },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => <Tag color={JOB_STATUS[s]?.color}>{JOB_STATUS[s]?.label || s}</Tag> },
    {
      title: 'Careers', dataIndex: 'publishedToCareers',
      render: (v) => v ? <Tag color="blue">Công khai</Tag> : <Tag>Ẩn</Tag>,
    },
    { title: 'Ngày đăng', dataIndex: 'createdAt', render: (v) => formatDate(v) },
  ];

  const candidateColumns = [
    { title: 'Họ và tên', dataIndex: 'fullName' },
    { title: 'Email', dataIndex: 'email' },
    { title: 'Vị trí', dataIndex: 'jobTitle' },
    { title: 'Số điện thoại', dataIndex: 'phone' },
    {
      title: 'Trạng thái', dataIndex: 'status',
      render: (s, r) => (
        <Select size="small" value={s} style={{ width: 130 }}
          onChange={(v) => handleStatusChange(r.id, v)}
          options={Object.entries(CANDIDATE_STATUS).map(([k, v]) => ({ value: k, label: v.label }))} />
      ),
    },
    { title: 'Ngày ứng tuyển', dataIndex: 'createdAt', render: (v) => formatDate(v) },
  ];

  const kanbanStatuses = ['NEW', 'SCREENING', 'INTERVIEW', 'OFFERED', 'HIRED', 'REJECTED'];

  return (
    <div>
      <div className="page-header">
        <div><h2>Tuyển dụng</h2><p>Quản lý tin tuyển dụng và ứng viên</p></div>
        <Space>
          <Button icon={<Plus size={16} />} onClick={() => setCandidateModal(true)}>Thêm ứng viên</Button>
          <Button type="primary" icon={<Plus size={16} />} onClick={() => setJobModal(true)} style={{ background: '#1e3a5f' }}>Đăng tin tuyển dụng</Button>
        </Space>
      </div>

      <Tabs items={[
        {
          key: 'jobs', label: 'Tin tuyển dụng',
          children: (
            <div className="data-table-card">
              <Table columns={jobColumns} dataSource={jobs} rowKey="id" loading={loading} pagination={false} />
            </div>
          ),
        },
        {
          key: 'candidates', label: 'Ứng viên',
          children: (
            <div className="data-table-card">
              <Table columns={candidateColumns} dataSource={candidates} rowKey="id" loading={loading} pagination={{ pageSize: 10 }} />
            </div>
          ),
        },
        {
          key: 'kanban', label: 'Quy trình',
          children: (
            <div className="kanban-board">
              {kanbanStatuses.map((status) => (
                <div key={status} className="kanban-column">
                  <h4>{CANDIDATE_STATUS[status]?.label} ({candidates.filter((c) => c.status === status).length})</h4>
                  {candidates.filter((c) => c.status === status).map((c) => (
                    <div key={c.id} className="kanban-card">
                      <div style={{ fontWeight: 500 }}>{c.fullName}</div>
                      <div style={{ fontSize: 12, color: '#64748b' }}>{c.jobTitle}</div>
                    </div>
                  ))}
                </div>
              ))}
            </div>
          ),
        },
      ]} />

      <Modal title="Đăng tin tuyển dụng mới" open={jobModal} onCancel={() => setJobModal(false)} footer={null}>
        <Form form={jobForm} layout="vertical" onFinish={handleCreateJob}>
          <Form.Item name="title" label="Tiêu đề công việc" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="departmentId" label="Phòng ban">
            <Select options={departments.map((d) => ({ value: d.id, label: d.name }))} allowClear />
          </Form.Item>
          <Form.Item name="positionId" label="Chức vụ">
            <Select options={positions.map((p) => ({ value: p.id, label: p.name }))} allowClear />
          </Form.Item>
          <Form.Item name="salaryRange" label="Mức lương"><Input placeholder="15M - 25M VND" /></Form.Item>
          <Form.Item name="description" label="Mô tả"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="requirement" label="Yêu cầu"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="publishedToCareers" label="Đăng lên trang Careers" valuePropName="checked">
            <Switch checkedChildren="Công khai" unCheckedChildren="Ẩn" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block style={{ background: '#1e3a5f' }}>Tạo tin tuyển dụng</Button>
        </Form>
      </Modal>

      <Modal title="Thêm ứng viên" open={candidateModal} onCancel={() => setCandidateModal(false)} footer={null}>
        <Form form={candidateForm} layout="vertical" onFinish={handleCreateCandidate}>
          <Form.Item name="jobId" label="Vị trí tuyển dụng" rules={[{ required: true }]}>
            <Select options={jobs.map((j) => ({ value: j.id, label: j.title }))} />
          </Form.Item>
          <Form.Item name="fullName" label="Họ và tên" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}><Input /></Form.Item>
          <Form.Item name="phone" label="Số điện thoại"><Input /></Form.Item>
          <Form.Item name="note" label="Ghi chú"><Input.TextArea rows={2} /></Form.Item>
          <Button type="primary" htmlType="submit" block style={{ background: '#1e3a5f' }}>Thêm ứng viên</Button>
        </Form>
      </Modal>
    </div>
  );
};

export default RecruitmentPage;
