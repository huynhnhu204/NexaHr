import { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, Button, message, Spin, Table, Modal, Form, Input, Select } from 'antd';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, LineChart, Line, Legend,
} from 'recharts';
import { TrendingUp, Users, Calendar, DollarSign, Download, Plus, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { useI18n } from '../../hooks/useI18n';
import { usePermission } from '../../hooks/usePermission';
import { formatCurrency } from '../../utils/formatCurrency';
import { formatDateTime } from '../../utils/formatDate';

const COLORS = ['#1E3A8A', '#2563EB', '#22C55E', '#F59E0B', '#8B5CF6', '#EF4444'];

const LEAVE_TYPE_LABELS = {
  ANNUAL_LEAVE: 'Nghỉ phép năm',
  SICK_LEAVE: 'Nghỉ ốm',
  UNPAID_LEAVE: 'Không lương',
};

const CANDIDATE_STATUS_LABELS = {
  NEW: 'Mới', SCREENING: 'Sàng lọc', INTERVIEW: 'Phỏng vấn',
  TECHNICAL_TEST: 'Bài test', OFFERED: 'Đề nghị', HIRED: 'Đã tuyển', REJECTED: 'Từ chối',
};

const AnalyticsPage = () => {
  const { t, lang } = useI18n();
  const { isAdmin } = usePermission();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [scheduled, setScheduled] = useState([]);
  const [schedModal, setSchedModal] = useState(false);
  const [schedForm] = Form.useForm();

  const fetch = () => {
    setLoading(true);
    const reqs = [axiosClient.get(ENDPOINTS.ANALYTICS.BASE)];
    if (isAdmin) reqs.push(axiosClient.get(ENDPOINTS.SCHEDULED_REPORTS));
    Promise.all(reqs)
      .then(([analyticsRes, schedRes]) => {
        setData(analyticsRes.data);
        if (schedRes) setScheduled(schedRes.data || []);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetch(); }, [isAdmin]);

  const handleExport = async () => {
    setExporting(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.ANALYTICS.EXPORT, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res]));
      const a = document.createElement('a');
      a.href = url;
      a.download = 'workforce-analytics.xlsx';
      a.click();
      window.URL.revokeObjectURL(url);
      message.success(lang === 'en' ? 'Export successful' : 'Xuất báo cáo thành công');
    } catch (err) {
      message.error(err.message);
    } finally {
      setExporting(false);
    }
  };

  const createScheduled = async (values) => {
    await axiosClient.post(ENDPOINTS.SCHEDULED_REPORTS, values);
    message.success(lang === 'en' ? 'Scheduled report created' : 'Tạo lịch báo cáo thành công');
    setSchedModal(false);
    schedForm.resetFields();
    fetch();
  };

  const deleteScheduled = (id) => {
    Modal.confirm({
      title: lang === 'en' ? 'Delete schedule?' : 'Xóa lịch báo cáo?',
      okType: 'danger',
      onOk: async () => {
        await axiosClient.delete(ENDPOINTS.SCHEDULED_REPORT(id));
        fetch();
      },
    });
  };

  if (loading) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;

  const o = data?.overview || {};

  const leaveData = (data?.leaveByType || []).map((d) => ({
    name: LEAVE_TYPE_LABELS[d.type] || d.type,
    value: d.count,
  }));

  const funnelData = (data?.recruitmentFunnel || []).map((d) => ({
    name: CANDIDATE_STATUS_LABELS[d.status] || d.status,
    count: d.count,
  }));

  return (
    <div>
      <PageHeader
        title={t('analytics.title')}
        subtitle={t('analytics.subtitle')}
        extra={
          <Button icon={<Download size={16} />} loading={exporting} onClick={handleExport}>
            {t('common.export')}
          </Button>
        }
      />

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card><Statistic title={t('analytics.turnover')} value={o.turnoverRate} suffix="%" prefix={<TrendingUp size={18} />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title={t('analytics.newHires')} value={o.newHiresThisMonth} prefix={<Users size={18} />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title={t('analytics.pendingLeave')} value={o.pendingLeaves} prefix={<Calendar size={18} />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title={t('analytics.payrollCost')} value={formatCurrency(o.payrollCostThisMonth)} prefix={<DollarSign size={18} />} /></Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title={lang === 'en' ? 'Hiring trend (6 months)' : 'Xu hướng tuyển dụng (6 tháng)'}>
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={data?.headcountTrend || []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="hires" fill="#1E3A8A" name={lang === 'en' ? 'New hires' : 'Tuyển mới'} radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={lang === 'en' ? 'Payroll trend' : 'Xu hướng chi phí lương'}>
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={data?.payrollTrend || []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis tickFormatter={(v) => `${(v / 1e6).toFixed(0)}M`} />
                <Tooltip formatter={(v) => formatCurrency(v)} />
                <Line type="monotone" dataKey="amount" stroke="#8B5CF6" strokeWidth={2} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} md={12} lg={8}>
          <Card title={lang === 'en' ? 'Leave by type' : 'Nghỉ phép theo loại'}>
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={leaveData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                  {leaveData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} md={12} lg={8}>
          <Card title={lang === 'en' ? 'Recruitment funnel' : 'Phễu tuyển dụng'}>
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={funnelData} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis type="number" allowDecimals={false} />
                <YAxis type="category" dataKey="name" width={90} tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="count" fill="#22C55E" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title={lang === 'en' ? 'Headcount by department' : 'Nhân sự theo phòng ban'}>
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={data?.departmentHeadcount || []} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={50} outerRadius={80}>
                  {(data?.departmentHeadcount || []).map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {isAdmin && (
        <Card
          title={lang === 'en' ? 'Scheduled reports' : 'Báo cáo theo lịch'}
          style={{ marginTop: 24 }}
          extra={<Button icon={<Plus size={16} />} onClick={() => setSchedModal(true)}>Thêm lịch</Button>}
        >
          <Table
            rowKey="id"
            dataSource={scheduled}
            pagination={false}
            locale={{ emptyText: 'Chưa có lịch báo cáo' }}
            columns={[
              { title: 'Tên', dataIndex: 'name' },
              { title: 'Tần suất', dataIndex: 'frequency' },
              { title: 'Email nhận', dataIndex: 'recipientEmails', ellipsis: true },
              { title: 'Lần chạy cuối', dataIndex: 'lastRunAt', render: (v) => v ? formatDateTime(v) : '—' },
              { title: '', key: 'a', render: (_, r) => r.active && <Button type="text" danger icon={<Trash2 size={16} />} onClick={() => deleteScheduled(r.id)} /> },
            ]}
          />
        </Card>
      )}

      <Modal title="Tạo lịch báo cáo email" open={schedModal} onCancel={() => setSchedModal(false)} footer={null} destroyOnClose>
        <Form form={schedForm} layout="vertical" onFinish={createScheduled} initialValues={{ frequency: 'WEEKLY', reportType: 'WORKFORCE' }}>
          <Form.Item name="name" label="Tên" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="frequency" label="Tần suất" rules={[{ required: true }]}>
            <Select options={[
              { value: 'DAILY', label: 'Hàng ngày' },
              { value: 'WEEKLY', label: 'Hàng tuần' },
              { value: 'MONTHLY', label: 'Hàng tháng' },
            ]} />
          </Form.Item>
          <Form.Item name="recipientEmails" label="Email nhận (phân cách bằng dấu phẩy)" rules={[{ required: true }]}>
            <Input placeholder="hr@company.com, ceo@company.com" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>Tạo lịch</Button>
        </Form>
      </Modal>
    </div>
  );
};

export default AnalyticsPage;
