import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import AuthLayout from '../components/layout/AuthLayout';
import MainLayout from '../components/layout/MainLayout';
import PrivateRoute from './PrivateRoute';
import RoleRoute from './RoleRoute';
import ErrorBoundary from '../components/common/ErrorBoundary';
import { PageSkeleton } from '../components/common/Skeleton';

const LoginPage = lazy(() => import('../features/auth/LoginPage'));
const ForgotPasswordPage = lazy(() => import('../features/auth/ForgotPasswordPage'));
const ResetPasswordPage = lazy(() => import('../features/auth/ResetPasswordPage'));
const ChangePasswordPage = lazy(() => import('../features/auth/ChangePasswordPage'));
const DashboardPage = lazy(() => import('../features/dashboard/DashboardPage'));
const EmployeeListPage = lazy(() => import('../features/employees/EmployeeListPage'));
const EmployeeDetailPage = lazy(() => import('../features/employees/EmployeeDetailPage'));
const DepartmentPage = lazy(() => import('../features/departments/DepartmentPage'));
const PositionPage = lazy(() => import('../features/positions/PositionPage'));
const AttendancePage = lazy(() => import('../features/attendance/AttendancePage'));
const AttendanceReportPage = lazy(() => import('../features/attendance/AttendanceReportPage'));
const LeavePage = lazy(() => import('../features/leave/LeavePage'));
const PayrollPage = lazy(() => import('../features/payroll/PayrollPage'));
const PayrollDetailPage = lazy(() => import('../features/payroll/PayrollDetailPage'));
const RecruitmentPage = lazy(() => import('../features/recruitment/RecruitmentPage'));
const PerformancePage = lazy(() => import('../features/performance/PerformancePage'));
const SettingsPage = lazy(() => import('../features/settings/SettingsPage'));
const OrgChartPage = lazy(() => import('../features/org-chart/OrgChartPage'));
const TrainingPage = lazy(() => import('../features/training/TrainingPage'));
const AssetsPage = lazy(() => import('../features/assets/AssetsPage'));
const ReportsPage = lazy(() => import('../features/reports/ReportsPage'));
const ActivityLogsPage = lazy(() => import('../features/activity-logs/ActivityLogsPage'));
const AuditLogsPage = lazy(() => import('../features/audit-logs/AuditLogsPage'));
const InterviewsPage = lazy(() => import('../features/interviews/InterviewsPage'));
const SubscriptionPage = lazy(() => import('../features/subscription/SubscriptionPage'));
const CompanySettingsPage = lazy(() => import('../features/settings/CompanySettingsPage'));
const IntegrationsPage = lazy(() => import('../features/integrations/IntegrationsPage'));
const AnalyticsPage = lazy(() => import('../features/analytics/AnalyticsPage'));
const AiCopilotPage = lazy(() => import('../features/ai/AiCopilotPage'));
const WorkflowsPage = lazy(() => import('../features/workflows/WorkflowsPage'));
const PermissionsPage = lazy(() => import('../features/permissions/PermissionsPage'));
const DataHubPage = lazy(() => import('../features/data-hub/DataHubPage'));
const CustomRolesPage = lazy(() => import('../features/custom-roles/CustomRolesPage'));
const AnnouncementsPage = lazy(() => import('../features/announcements/AnnouncementsPage'));
const MobileLayout = lazy(() => import('../features/mobile/MobileLayout'));
const MobileHubPage = lazy(() => import('../features/mobile/MobileHubPage'));
const MobileMorePage = lazy(() => import('../features/mobile/MobileMorePage'));
const PublicLayout = lazy(() => import('../features/careers/PublicLayout'));
const CareersPortalPage = lazy(() => import('../features/careers/CareersPortalPage'));
const JobDetailPublicPage = lazy(() => import('../features/careers/JobDetailPublicPage'));

const Lazy = ({ children }) => (
  <Suspense fallback={<PageSkeleton />}>{children}</Suspense>
);

const AppRoutes = () => {
  const { isAuthenticated } = useSelector((state) => state.auth);

  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Routes>
          <Route element={<AuthLayout />}>
            <Route path="/login" element={
              isAuthenticated ? <Navigate to="/dashboard" /> : <Lazy><LoginPage /></Lazy>
            } />
            <Route path="/forgot-password" element={<Lazy><ForgotPasswordPage /></Lazy>} />
            <Route path="/reset-password" element={<Lazy><ResetPasswordPage /></Lazy>} />
          </Route>

          <Route element={<Lazy><PublicLayout /></Lazy>}>
            <Route path="/careers/:companyCode" element={<Lazy><CareersPortalPage /></Lazy>} />
            <Route path="/careers/:companyCode/jobs/:jobId" element={<Lazy><JobDetailPublicPage /></Lazy>} />
          </Route>

          <Route element={<PrivateRoute />}>
            <Route element={<MainLayout />}>
              <Route path="/dashboard" element={<Lazy><DashboardPage /></Lazy>} />
              <Route path="/attendance" element={<Lazy><AttendancePage /></Lazy>} />
              <Route path="/attendance/reports" element={<Lazy><AttendanceReportPage /></Lazy>} />
              <Route path="/leaves" element={<Lazy><LeavePage /></Lazy>} />
              <Route path="/announcements" element={<Lazy><AnnouncementsPage /></Lazy>} />
              <Route path="/settings" element={<Lazy><SettingsPage /></Lazy>} />
              <Route path="/change-password" element={<Lazy><ChangePasswordPage /></Lazy>} />

              <Route element={<RoleRoute roles={['ADMIN']} />}>
                <Route path="/settings/subscription" element={<Lazy><SubscriptionPage /></Lazy>} />
                <Route path="/settings/company" element={<Lazy><CompanySettingsPage /></Lazy>} />
                <Route path="/settings/integrations" element={<Lazy><IntegrationsPage /></Lazy>} />
                <Route path="/settings/workflows" element={<Lazy><WorkflowsPage /></Lazy>} />
                <Route path="/settings/permissions" element={<Lazy><PermissionsPage /></Lazy>} />
                <Route path="/settings/data" element={<Lazy><DataHubPage /></Lazy>} />
                <Route path="/settings/custom-roles" element={<Lazy><CustomRolesPage /></Lazy>} />
              </Route>

              <Route element={<RoleRoute roles={['ADMIN', 'HR', 'MANAGER']} />}>
                <Route path="/employees" element={<Lazy><EmployeeListPage /></Lazy>} />
                <Route path="/employees/:id" element={<Lazy><EmployeeDetailPage /></Lazy>} />
                <Route path="/org-chart" element={<Lazy><OrgChartPage /></Lazy>} />
              </Route>

              <Route path="/payroll" element={<Lazy><PayrollPage /></Lazy>} />
              <Route path="/payroll/:id" element={<Lazy><PayrollDetailPage /></Lazy>} />

              <Route element={<RoleRoute roles={['ADMIN', 'HR']} />}>
                <Route path="/departments" element={<Lazy><DepartmentPage /></Lazy>} />
                <Route path="/positions" element={<Lazy><PositionPage /></Lazy>} />
                <Route path="/recruitment" element={<Lazy><RecruitmentPage /></Lazy>} />
                <Route path="/interviews" element={<Lazy><InterviewsPage /></Lazy>} />
                <Route path="/training" element={<Lazy><TrainingPage /></Lazy>} />
                <Route path="/assets" element={<Lazy><AssetsPage /></Lazy>} />
              </Route>

              <Route element={<RoleRoute roles={['ADMIN', 'HR', 'MANAGER']} />}>
                <Route path="/performance" element={<Lazy><PerformancePage /></Lazy>} />
                <Route path="/reports" element={<Lazy><ReportsPage /></Lazy>} />
                <Route path="/analytics" element={<Lazy><AnalyticsPage /></Lazy>} />
                <Route path="/ai-copilot" element={<Lazy><AiCopilotPage /></Lazy>} />
              </Route>

              <Route element={<RoleRoute roles={['ADMIN']} />}>
                <Route path="/activity-logs" element={<Lazy><ActivityLogsPage /></Lazy>} />
                <Route path="/audit-logs" element={<Lazy><AuditLogsPage /></Lazy>} />
              </Route>
            </Route>

            <Route element={<Lazy><MobileLayout /></Lazy>}>
              <Route path="/mobile" element={<Lazy><MobileHubPage /></Lazy>} />
              <Route path="/mobile/attendance" element={<Lazy><AttendancePage /></Lazy>} />
              <Route path="/mobile/leaves" element={<Lazy><LeavePage /></Lazy>} />
              <Route path="/mobile/payroll" element={<Lazy><PayrollPage /></Lazy>} />
              <Route path="/mobile/more" element={<Lazy><MobileMorePage /></Lazy>} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to={isAuthenticated ? '/dashboard' : '/login'} />} />
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
};

export default AppRoutes;
