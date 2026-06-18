import { useEffect, useState } from 'react';
import {
  Table, Button, Tabs, Modal, Form, Input, DatePicker, InputNumber,
  Select, Space, message,
} from 'antd';
import { Plus, UserPlus } from 'lucide-react';
import dayjs from 'dayjs';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { COURSE_STATUS, ENROLLMENT_STATUS } from '../../utils/constants';
import { formatDate, formatDateTime } from '../../utils/formatDate';
import useTraining from './hooks/useTraining';

const TrainingPage = () => {
  const {
    courses, enrollments, loading, courseTotal, enrollmentTotal,
    coursePage, setCoursePage, enrollmentPage, setEnrollmentPage,
    createCourse, enrollEmployee, updateEnrollmentStatus,
  } = useTraining();
  const [courseModalOpen, setCourseModalOpen] = useState(false);
  const [enrollModalOpen, setEnrollModalOpen] = useState(false);
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [courseForm] = Form.useForm();
  const [enrollForm] = Form.useForm();

  useEffect(() => {
    axiosClient.get(ENDPOINTS.EMPLOYEES.BASE, { params: { size: 100 } })
      .then((res) => setEmployees(res.data?.content || []));
  }, []);

  const handleCreateCourse = async () => {
    const values = await courseForm.validateFields();
    const payload = {
      ...values,
      startDate: values.startDate?.format('YYYY-MM-DD'),
      endDate: values.endDate?.format('YYYY-MM-DD'),
      status: values.status || 'ACTIVE',
    };
    try {
      await createCourse(payload);
      message.success('Tạo khóa học thành công');
      setCourseModalOpen(false);
      courseForm.resetFields();
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleEnroll = async () => {
    const values = await enrollForm.validateFields();
    try {
      await enrollEmployee(selectedCourse.id, values.employeeId);
      message.success('Đăng ký khóa học thành công');
      setEnrollModalOpen(false);
    } catch (err) {
      message.error(err.message);
    }
  };

  const courseColumns = [
    { title: 'Tên khóa học', dataIndex: 'title' },
    { title: 'Giảng viên', dataIndex: 'instructor', render: (v) => v || '-' },
    { title: 'Bắt đầu', dataIndex: 'startDate', render: (v) => formatDate(v) },
    { title: 'Kết thúc', dataIndex: 'endDate', render: (v) => formatDate(v) },
    { title: 'Số học viên', dataIndex: 'enrollmentCount', render: (v) => v || 0 },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => <StatusBadge status={s} map={COURSE_STATUS} /> },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, record) => (
        <Button type="text" icon={<UserPlus size={16} />} onClick={() => { setSelectedCourse(record); enrollForm.resetFields(); setEnrollModalOpen(true); }}>
          Đăng ký
        </Button>
      ),
    },
  ];

  const enrollmentColumns = [
    { title: 'Khóa học', dataIndex: 'courseTitle' },
    { title: 'Nhân viên', dataIndex: 'employeeName' },
    { title: 'Ngày đăng ký', dataIndex: 'enrolledAt', render: (v) => formatDateTime(v) },
    { title: 'Điểm', dataIndex: 'score', render: (v) => v ?? '-' },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => <StatusBadge status={s} map={ENROLLMENT_STATUS} /> },
    {
      title: 'Cập nhật',
      key: 'update',
      render: (_, record) => (
        <Select
          size="small"
          value={record.status}
          style={{ width: 140 }}
          onChange={(v) => updateEnrollmentStatus(record.id, v, record.score)}
          options={Object.entries(ENROLLMENT_STATUS).map(([k, val]) => ({ value: k, label: val.label }))}
        />
      ),
    },
  ];

  const tabItems = [
    {
      key: 'courses',
      label: 'Khóa học',
      children: (
        <div className="data-table-card table-responsive">
          <Table columns={courseColumns} dataSource={courses} rowKey="id" loading={loading}
            pagination={{ current: coursePage + 1, total: courseTotal, pageSize: 10, onChange: (p) => setCoursePage(p - 1) }}
            locale={{ emptyText: <EmptyState title="Chưa có khóa học" /> }} />
        </div>
      ),
    },
    {
      key: 'enrollments',
      label: 'Đăng ký học',
      children: (
        <div className="data-table-card table-responsive">
          <Table columns={enrollmentColumns} dataSource={enrollments} rowKey="id" loading={loading}
            pagination={{ current: enrollmentPage + 1, total: enrollmentTotal, pageSize: 10, onChange: (p) => setEnrollmentPage(p - 1) }}
            locale={{ emptyText: <EmptyState title="Chưa có đăng ký" /> }} />
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Đào tạo"
        subtitle="Quản lý khóa học nội bộ và tiến độ học tập"
        extra={(
          <Button type="primary" icon={<Plus size={16} />} onClick={() => { courseForm.resetFields(); setCourseModalOpen(true); }}
            style={{ background: '#1E3A8A' }}>
            Thêm khóa học
          </Button>
        )}
      />

      <Tabs items={tabItems} />

      <Modal title="Thêm khóa học" open={courseModalOpen} onCancel={() => setCourseModalOpen(false)} onOk={handleCreateCourse} okText="Tạo">
        <Form form={courseForm} layout="vertical">
          <Form.Item name="title" label="Tên khóa học" rules={[{ required: true, message: 'Nhập tên khóa học' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="instructor" label="Giảng viên">
            <Input />
          </Form.Item>
          <Form.Item name="startDate" label="Ngày bắt đầu">
            <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" />
          </Form.Item>
          <Form.Item name="endDate" label="Ngày kết thúc">
            <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" />
          </Form.Item>
          <Form.Item name="maxParticipants" label="Số lượng tối đa">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="status" label="Trạng thái" initialValue="ACTIVE">
            <Select options={Object.entries(COURSE_STATUS).map(([k, v]) => ({ value: k, label: v.label }))} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`Đăng ký — ${selectedCourse?.title || ''}`} open={enrollModalOpen} onCancel={() => setEnrollModalOpen(false)} onOk={handleEnroll} okText="Đăng ký">
        <Form form={enrollForm} layout="vertical">
          <Form.Item name="employeeId" label="Nhân viên" rules={[{ required: true, message: 'Chọn nhân viên' }]}>
            <Select showSearch optionFilterProp="label"
              options={employees.map((e) => ({ value: e.id, label: e.fullName }))} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TrainingPage;
