import { Row, Col, Card } from 'antd';
import { BarChart3, Users, Clock, DollarSign, UserPlus, Target } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../../components/common/PageHeader';

const REPORTS = [
  { key: 'employees', title: 'Báo cáo nhân sự', desc: 'Thống kê nhân viên theo phòng ban, trạng thái', icon: Users, color: '#2563EB', path: '/employees' },
  { key: 'attendance', title: 'Báo cáo chấm công', desc: 'Đi muộn, vắng mặt, overtime', icon: Clock, color: '#22C55E', path: '/attendance/reports' },
  { key: 'payroll', title: 'Báo cáo lương', desc: 'Chi phí lương, thuế, bảo hiểm', icon: DollarSign, color: '#8B5CF6', path: '/payroll' },
  { key: 'recruitment', title: 'Báo cáo tuyển dụng', desc: 'Pipeline, thời gian tuyển, nguồn ứng viên', icon: UserPlus, color: '#F59E0B', path: '/recruitment' },
  { key: 'kpi', title: 'Báo cáo KPI', desc: 'Đánh giá hiệu suất theo chu kỳ', icon: Target, color: '#EF4444', path: '/performance' },
];

const ReportsPage = () => {
  const navigate = useNavigate();
  return (
    <div>
      <PageHeader title="Trung tâm báo cáo" subtitle="Báo cáo tổng hợp theo module" />
      <Row gutter={[16, 16]}>
        {REPORTS.map((r) => {
          const Icon = r.icon;
          return (
            <Col xs={24} sm={12} lg={8} key={r.key}>
              <Card className="stat-card" hoverable onClick={() => navigate(r.path)} style={{ cursor: 'pointer' }}>
                <div className="stat-icon" style={{ background: `${r.color}15` }}>
                  <Icon size={24} color={r.color} />
                </div>
                <div style={{ fontSize: 16, fontWeight: 600, marginBottom: 4 }}>{r.title}</div>
                <div style={{ fontSize: 13, color: '#64748B' }}>{r.desc}</div>
              </Card>
            </Col>
          );
        })}
      </Row>
    </div>
  );
};

export default ReportsPage;
