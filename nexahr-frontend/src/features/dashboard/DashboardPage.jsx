import { useEffect, useState } from 'react';
import { Users, UserPlus, CalendarDays, DollarSign, Briefcase, ClipboardList } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { formatCurrency } from '../../utils/formatCurrency';
import PageHeader from '../../components/common/PageHeader';
import { SkeletonDashboard } from '../../components/common/Skeleton';
import EmployeeChart from '../../components/charts/EmployeeChart';
import PayrollChart from '../../components/charts/PayrollChart';
import RecruitmentChart from '../../components/charts/RecruitmentChart';
import LeaveAnalyticsChart from '../../components/charts/LeaveAnalyticsChart';

const StatCard = ({ icon: Icon, label, value, color, bg, compact }) => (
  <div className={`stat-card${compact ? ' stat-card--compact' : ''}`}>
    <div className="stat-icon" style={{ background: bg }}>
      <Icon size={22} color={color} />
    </div>
    <div className="stat-body">
      <div className={`stat-value${compact ? ' stat-value--compact' : ''}`}>{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  </div>
);

const ACTIVITY_COLORS = { employee: '#2563EB', leave: '#F59E0B', payroll: '#8B5CF6', default: '#64748B' };

const DashboardPage = () => {
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState(null);
  const [employeeChart, setEmployeeChart] = useState([]);
  const [payrollChart, setPayrollChart] = useState([]);
  const [recruitmentChart, setRecruitmentChart] = useState([]);
  const [leaveChart, setLeaveChart] = useState([]);
  const [activities, setActivities] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const results = await Promise.allSettled([
          axiosClient.get(ENDPOINTS.DASHBOARD.SUMMARY),
          axiosClient.get(ENDPOINTS.DASHBOARD.EMPLOYEE_CHART),
          axiosClient.get(ENDPOINTS.DASHBOARD.PAYROLL_CHART),
          axiosClient.get(ENDPOINTS.DASHBOARD.RECRUITMENT_CHART),
          axiosClient.get(ENDPOINTS.DASHBOARD.LEAVE_CHART),
          axiosClient.get(ENDPOINTS.DASHBOARD.ACTIVITIES),
        ]);
        const unwrap = (r) => r?.data ?? r;
        if (results[0].status === 'fulfilled') setSummary(unwrap(results[0].value));
        if (results[1].status === 'fulfilled') setEmployeeChart(unwrap(results[1].value) || []);
        if (results[2].status === 'fulfilled') setPayrollChart(unwrap(results[2].value) || []);
        if (results[3].status === 'fulfilled') setRecruitmentChart(unwrap(results[3].value) || []);
        if (results[4].status === 'fulfilled') setLeaveChart(unwrap(results[4].value) || []);
        if (results[5].status === 'fulfilled') setActivities(unwrap(results[5].value) || []);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <SkeletonDashboard />;

  const activityList = activities.length ? activities : summary?.recentActivities || [];

  return (
    <div className="dashboard-page">
      <PageHeader title="Tổng quan" subtitle="Tình hình nhân sự toàn công ty" />

      <section className="dashboard-stats-grid" aria-label="Thống kê tổng quan">
        <StatCard icon={Users} label="Tổng nhân viên" value={summary?.totalEmployees ?? 0} color="#1E3A8A" bg="#EFF6FF" />
        <StatCard icon={UserPlus} label="Nhân viên mới" value={summary?.newEmployeesThisMonth ?? 0} color="#22C55E" bg="#F0FDF4" />
        <StatCard icon={CalendarDays} label="Nghỉ phép chờ duyệt" value={summary?.pendingLeaveRequests ?? 0} color="#F59E0B" bg="#FFFBEB" />
        <StatCard icon={DollarSign} label="Quỹ lương tháng" value={formatCurrency(summary?.totalPayrollThisMonth)} color="#8B5CF6" bg="#F5F3FF" compact />
        <StatCard icon={Briefcase} label="Tuyển dụng đang mở" value={summary?.activeRecruitment ?? 0} color="#2563EB" bg="#EFF6FF" />
        <StatCard icon={ClipboardList} label="Vị trí tuyển dụng" value={summary?.openPositions ?? 0} color="#EF4444" bg="#FEF2F2" />
      </section>

      <section className="dashboard-panel-grid" aria-label="Biểu đồ">
        <div className="chart-card">
          <h4>Nhân sự theo phòng ban</h4>
          <div className="chart-card-body"><EmployeeChart data={employeeChart} /></div>
        </div>
        <div className="chart-card">
          <h4>Xu hướng bảng lương</h4>
          <div className="chart-card-body"><PayrollChart data={payrollChart} /></div>
        </div>
        <div className="chart-card">
          <h4>Hoạt động tuyển dụng</h4>
          <div className="chart-card-body"><RecruitmentChart data={recruitmentChart} /></div>
        </div>
      </section>

      <section className="dashboard-panel-grid dashboard-panel-grid--bottom" aria-label="Phân tích và hoạt động">
        <div className="chart-card">
          <h4>Phân tích nghỉ phép</h4>
          <div className="chart-card-body"><LeaveAnalyticsChart data={leaveChart} /></div>
        </div>
        <div className="widget-card">
          <h4>Hoạt động gần đây</h4>
          <div className="widget-card-body">
            {activityList.length ? activityList.map((a, i) => (
              <div key={i} className="activity-item">
                <div className="activity-dot" style={{ background: ACTIVITY_COLORS[a.type] || ACTIVITY_COLORS.default }} />
                <div className="activity-content">
                  <div className="activity-message">{a.message}</div>
                  <div className="activity-time">{a.time}</div>
                </div>
              </div>
            )) : (
              <p className="dashboard-empty">Chưa có hoạt động gần đây</p>
            )}
          </div>
        </div>
        <div className="widget-card widget-card--accent">
          <h4>Chờ phê duyệt</h4>
          <div className="widget-card-body">
            <div className="pending-approval-card">
              <div className="pending-approval-count">{summary?.pendingLeaveRequests ?? 0}</div>
              <div className="pending-approval-label">đơn nghỉ phép chờ duyệt</div>
              <div className="pending-approval-hint">Cần xử lý sớm</div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default DashboardPage;
