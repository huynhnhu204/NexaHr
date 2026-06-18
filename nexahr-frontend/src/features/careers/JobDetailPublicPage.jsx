import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Card, Button, Form, Input, message, Spin, Tag, Row, Col } from 'antd';
import { ArrowLeft, Send } from 'lucide-react';
import axios from 'axios';
import { API_BASE_URL } from '../../utils/constants';
import { JOB_STATUS } from '../../utils/constants';

const JobDetailPublicPage = () => {
  const { companyCode, jobId } = useParams();
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [applied, setApplied] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    axios.get(`${API_BASE_URL}/public/careers/${companyCode}/jobs/${jobId}`)
      .then((res) => setJob(res.data?.data ?? res.data))
      .finally(() => setLoading(false));
  }, [companyCode, jobId]);

  const onApply = async (values) => {
    setSubmitting(true);
    try {
      await axios.post(`${API_BASE_URL}/public/careers/${companyCode}/jobs/${jobId}/apply`, values);
      message.success('Đã gửi hồ sơ ứng tuyển thành công!');
      setApplied(true);
      form.resetFields();
    } catch (err) {
      message.error(err.response?.data?.message || 'Gửi hồ sơ thất bại');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading && !job) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;
  if (!job) return <div>Không tìm thấy tin tuyển dụng</div>;

  return (
    <div className="careers-portal">
      <Link to={`/careers/${companyCode}`} className="careers-back">
        <ArrowLeft size={16} /> Quay lại danh sách
      </Link>

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={14}>
          <Card className="careers-detail-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
              <h1 style={{ fontSize: 26, margin: 0 }}>{job.title}</h1>
              <Tag color={JOB_STATUS[job.status]?.color}>{JOB_STATUS[job.status]?.label}</Tag>
            </div>
            {job.salaryRange && <p className="careers-salary" style={{ fontSize: 16 }}>{job.salaryRange}</p>}
            {job.departmentName && <p style={{ color: '#64748B' }}>Phòng ban: {job.departmentName}</p>}

            <h4 style={{ marginTop: 24 }}>Mô tả công việc</h4>
            <p style={{ whiteSpace: 'pre-wrap', lineHeight: 1.7 }}>{job.description || '—'}</p>

            <h4 style={{ marginTop: 20 }}>Yêu cầu</h4>
            <p style={{ whiteSpace: 'pre-wrap', lineHeight: 1.7 }}>{job.requirement || '—'}</p>
          </Card>
        </Col>

        <Col xs={24} lg={10}>
          <Card title="Ứng tuyển ngay" className="careers-apply-card">
            {applied ? (
              <div style={{ textAlign: 'center', padding: 24 }}>
                <div style={{ fontSize: 48, marginBottom: 12 }}>✅</div>
                <h4>Đã gửi hồ sơ!</h4>
                <p style={{ color: '#64748B' }}>Chúng tôi sẽ liên hệ với bạn sớm.</p>
              </div>
            ) : (
              <Form form={form} layout="vertical" onFinish={onApply}>
                <Form.Item name="fullName" label="Họ và tên" rules={[{ required: true, message: 'Nhập họ tên' }]}>
                  <Input placeholder="Nguyễn Văn A" />
                </Form.Item>
                <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
                  <Input placeholder="email@example.com" />
                </Form.Item>
                <Form.Item name="phone" label="Số điện thoại">
                  <Input placeholder="0901234567" />
                </Form.Item>
                <Form.Item name="note" label="Giới thiệu bản thân">
                  <Input.TextArea rows={4} placeholder="Kinh nghiệm, kỹ năng..." />
                </Form.Item>
                <Button type="primary" htmlType="submit" block loading={submitting} icon={<Send size={16} />}
                  style={{ background: '#1E3A8A' }}>
                  Gửi hồ sơ ứng tuyển
                </Button>
              </Form>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default JobDetailPublicPage;
