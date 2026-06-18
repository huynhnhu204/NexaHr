export const designTokens = {
  colors: {
    primary: '#1E3A8A',
    primaryLight: '#2563EB',
    success: '#22C55E',
    warning: '#F59E0B',
    danger: '#EF4444',
    background: '#F8FAFC',
    surface: '#FFFFFF',
    text: '#0F172A',
    textSecondary: '#64748B',
    border: '#E2E8F0',
    sidebarBg: '#0F172A',
    sidebarHover: '#1E293B',
    sidebarActive: '#1E3A8A',
  },
  radius: { sm: 6, md: 8, lg: 12, xl: 16 },
  shadow: {
    sm: '0 1px 2px rgba(15, 23, 42, 0.05)',
    md: '0 4px 12px rgba(15, 23, 42, 0.08)',
    lg: '0 12px 40px rgba(15, 23, 42, 0.12)',
  },
};

export const ROLE_LABELS = {
  ADMIN: 'Quản trị viên',
  HR: 'Nhân sự',
  MANAGER: 'Quản lý',
  EMPLOYEE: 'Nhân viên',
};

export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

export const ROLES = { ADMIN: 'ADMIN', HR: 'HR', MANAGER: 'MANAGER', EMPLOYEE: 'EMPLOYEE' };

export const EMPLOYMENT_STATUS = {
  ACTIVE: { label: 'Đang làm việc', color: 'green' },
  PROBATION: { label: 'Thử việc', color: 'orange' },
  RESIGNED: { label: 'Đã nghỉ', color: 'red' },
};

export const LEAVE_STATUS = {
  PENDING: { label: 'Chờ duyệt', color: 'gold' },
  APPROVED: { label: 'Đã duyệt', color: 'green' },
  REJECTED: { label: 'Từ chối', color: 'red' },
  CANCELLED: { label: 'Đã hủy', color: 'default' },
};

export const LEAVE_TYPES = {
  ANNUAL_LEAVE: 'Nghỉ phép năm',
  SICK_LEAVE: 'Nghỉ ốm',
  UNPAID_LEAVE: 'Nghỉ không lương',
};

export const ATTENDANCE_STATUS = {
  ON_TIME: { label: 'Đúng giờ', color: 'green' },
  LATE: { label: 'Đi muộn', color: 'orange' },
  EARLY_LEAVE: { label: 'Về sớm', color: 'gold' },
  ABSENT: { label: 'Vắng mặt', color: 'red' },
};

export const CANDIDATE_STATUS = {
  NEW: { label: 'Mới', color: 'blue' },
  SCREENING: { label: 'Sàng lọc', color: 'cyan' },
  INTERVIEW: { label: 'Phỏng vấn', color: 'purple' },
  TECHNICAL_TEST: { label: 'Bài test', color: 'geekblue' },
  OFFERED: { label: 'Đề nghị', color: 'gold' },
  HIRED: { label: 'Đã tuyển', color: 'green' },
  REJECTED: { label: 'Từ chối', color: 'red' },
};

export const JOB_STATUS = {
  OPEN: { label: 'Đang tuyển', color: 'green' },
  CLOSED: { label: 'Đã đóng', color: 'default' },
  DRAFT: { label: 'Nháp', color: 'gold' },
};

export const PAYROLL_STATUS = {
  DRAFT: { label: 'Nháp', color: 'default' },
  APPROVED: { label: 'Đã duyệt', color: 'green' },
  PAID: { label: 'Đã thanh toán', color: 'blue' },
};

export const INTERVIEW_STATUS = {
  SCHEDULED: { label: 'Đã lên lịch', color: 'blue' },
  COMPLETED: { label: 'Hoàn thành', color: 'green' },
  CANCELLED: { label: 'Đã hủy', color: 'red' },
};

export const INTERVIEW_MODE = {
  ONLINE: { label: 'Trực tuyến', color: 'cyan' },
  OFFLINE: { label: 'Trực tiếp', color: 'purple' },
};

export const ASSET_TYPE = {
  LAPTOP: { label: 'Laptop', color: 'blue' },
  PHONE: { label: 'Điện thoại', color: 'cyan' },
  MONITOR: { label: 'Màn hình', color: 'geekblue' },
  VEHICLE: { label: 'Phương tiện', color: 'orange' },
  FURNITURE: { label: 'Nội thất', color: 'gold' },
  OTHER: { label: 'Khác', color: 'default' },
};

export const ASSET_STATUS = {
  AVAILABLE: { label: 'Sẵn sàng', color: 'green' },
  ASSIGNED: { label: 'Đã cấp phát', color: 'blue' },
  MAINTENANCE: { label: 'Bảo trì', color: 'orange' },
  RETIRED: { label: 'Thanh lý', color: 'default' },
};

export const COURSE_STATUS = {
  ACTIVE: { label: 'Đang mở', color: 'green' },
  INACTIVE: { label: 'Tạm dừng', color: 'default' },
  COMPLETED: { label: 'Đã kết thúc', color: 'blue' },
};

export const ENROLLMENT_STATUS = {
  ENROLLED: { label: 'Đã đăng ký', color: 'blue' },
  IN_PROGRESS: { label: 'Đang học', color: 'orange' },
  COMPLETED: { label: 'Hoàn thành', color: 'green' },
  CANCELLED: { label: 'Đã hủy', color: 'red' },
};

export const DOCUMENT_TYPE = {
  AVATAR: { label: 'Ảnh đại diện', color: 'blue' },
  CV: { label: 'CV', color: 'cyan' },
  CONTRACT: { label: 'Hợp đồng', color: 'green' },
  CERTIFICATE: { label: 'Chứng chỉ', color: 'gold' },
  NATIONAL_ID: { label: 'CMND/CCCD', color: 'purple' },
  OTHER: { label: 'Khác', color: 'default' },
};

export const ACTION_BADGE = {
  CREATE: { label: 'Tạo mới', color: 'green' },
  UPDATE: { label: 'Cập nhật', color: 'blue' },
  DELETE: { label: 'Xóa', color: 'red' },
  LOGIN: { label: 'Đăng nhập', color: 'cyan' },
  LOGOUT: { label: 'Đăng xuất', color: 'default' },
  APPROVE: { label: 'Phê duyệt', color: 'gold' },
  REJECT: { label: 'Từ chối', color: 'red' },
  IMPORT: { label: 'Nhập dữ liệu', color: 'geekblue' },
  EXPORT: { label: 'Xuất dữ liệu', color: 'purple' },
  UPLOAD: { label: 'Tải lên', color: 'geekblue' },
};

export const PERFORMANCE_REVIEW_STATUS = {
  DRAFT: { label: 'Nháp', color: 'default' },
  PENDING_SELF: { label: 'Chờ NV tự đánh giá', color: 'gold' },
  PENDING_MANAGER: { label: 'Chờ quản lý', color: 'orange' },
  COMPLETED: { label: 'Hoàn tất', color: 'green' },
};

export const PERFORMANCE_RATINGS = {
  EXCELLENT: { label: 'Xuất sắc', color: 'green' },
  GOOD: { label: 'Tốt', color: 'blue' },
  AVERAGE: { label: 'Trung bình', color: 'gold' },
  POOR: { label: 'Yếu', color: 'red' },
};

/** Sidebar navigation — grouped enterprise structure */
export const NAVIGATION = [
  { key: '/dashboard', label: 'Tổng quan', icon: 'LayoutDashboard', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'] },
  {
    key: 'hr', label: 'Nhân sự', icon: 'Users', roles: ['ADMIN', 'HR', 'MANAGER'],
    children: [
      { key: '/employees', label: 'Nhân viên', roles: ['ADMIN', 'HR', 'MANAGER'] },
      { key: '/departments', label: 'Phòng ban', roles: ['ADMIN', 'HR'] },
      { key: '/positions', label: 'Chức vụ', roles: ['ADMIN', 'HR'] },
      { key: '/org-chart', label: 'Sơ đồ tổ chức', roles: ['ADMIN', 'HR', 'MANAGER'] },
    ],
  },
  {
    key: 'attendance', label: 'Chấm công', icon: 'Clock', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'],
    children: [
      { key: '/attendance', label: 'Bản ghi chấm công', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'] },
      { key: '/attendance/reports', label: 'Báo cáo chấm công', roles: ['ADMIN', 'HR', 'MANAGER'] },
    ],
  },
  { key: '/leaves', label: 'Nghỉ phép', icon: 'CalendarDays', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'] },
  { key: '/payroll', label: 'Bảng lương', icon: 'DollarSign', roles: ['ADMIN', 'HR', 'EMPLOYEE'] },
  {
    key: 'recruitment', label: 'Tuyển dụng', icon: 'UserPlus', roles: ['ADMIN', 'HR'],
    children: [
      { key: '/recruitment', label: 'Tin tuyển dụng', roles: ['ADMIN', 'HR'] },
      { key: '/interviews', label: 'Lịch phỏng vấn', roles: ['ADMIN', 'HR'] },
    ],
  },
  { key: '/performance', label: 'Đánh giá KPI', icon: 'Target', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { key: '/training', label: 'Đào tạo', icon: 'GraduationCap', roles: ['ADMIN', 'HR'] },
  { key: '/assets', label: 'Tài sản', icon: 'Laptop', roles: ['ADMIN', 'HR'] },
  { key: '/reports', label: 'Báo cáo', icon: 'BarChart3', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { key: '/analytics', label: 'Phân tích HR', icon: 'LineChart', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { key: '/ai-copilot', label: 'AI Copilot', icon: 'Sparkles', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { key: '/announcements', label: 'Bảng tin', icon: 'Megaphone', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'] },
  { key: '/activity-logs', label: 'Nhật ký hoạt động', icon: 'ScrollText', roles: ['ADMIN'] },
  { key: '/audit-logs', label: 'Nhật ký bảo mật', icon: 'Shield', roles: ['ADMIN'] },
  { key: '/settings', label: 'Cài đặt', icon: 'Settings', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'] },
];

export const BREADCRUMB_MAP = {
  '/dashboard': [{ title: 'Tổng quan' }],
  '/employees': [{ title: 'Nhân sự' }, { title: 'Nhân viên' }],
  '/departments': [{ title: 'Nhân sự' }, { title: 'Phòng ban' }],
  '/positions': [{ title: 'Nhân sự' }, { title: 'Chức vụ' }],
  '/org-chart': [{ title: 'Nhân sự' }, { title: 'Sơ đồ tổ chức' }],
  '/attendance': [{ title: 'Chấm công' }, { title: 'Bản ghi' }],
  '/attendance/reports': [{ title: 'Chấm công' }, { title: 'Báo cáo' }],
  '/leaves': [{ title: 'Nghỉ phép' }],
  '/payroll': [{ title: 'Bảng lương' }],
  '/recruitment': [{ title: 'Tuyển dụng' }],
  '/interviews': [{ title: 'Tuyển dụng' }, { title: 'Phỏng vấn' }],
  '/performance': [{ title: 'Đánh giá KPI' }],
  '/training': [{ title: 'Đào tạo' }],
  '/assets': [{ title: 'Tài sản' }],
  '/reports': [{ title: 'Báo cáo' }],
  '/analytics': [{ title: 'Phân tích HR' }],
  '/ai-copilot': [{ title: 'AI Copilot' }],
  '/announcements': [{ title: 'Bảng tin nội bộ' }],
  '/mobile': [{ title: 'Mobile' }],
  '/activity-logs': [{ title: 'Nhật ký hoạt động' }],
  '/audit-logs': [{ title: 'Nhật ký bảo mật' }],
  '/settings': [{ title: 'Cài đặt' }],
  '/settings/subscription': [{ title: 'Cài đặt' }, { title: 'Gói đăng ký' }],
  '/settings/company': [{ title: 'Cài đặt' }, { title: 'Cài đặt công ty' }],
  '/settings/integrations': [{ title: 'Cài đặt' }, { title: 'Tích hợp' }],
  '/settings/workflows': [{ title: 'Cài đặt' }, { title: 'Quy trình tự động' }],
  '/settings/permissions': [{ title: 'Cài đặt' }, { title: 'Phân quyền' }],
  '/settings/data': [{ title: 'Cài đặt' }, { title: 'Trung tâm dữ liệu' }],
  '/settings/custom-roles': [{ title: 'Cài đặt' }, { title: 'Vai trò tùy chỉnh' }],
  '/change-password': [{ title: 'Cài đặt' }, { title: 'Đổi mật khẩu' }],
  '/forgot-password': [{ title: 'Quên mật khẩu' }],
  '/reset-password': [{ title: 'Đặt lại mật khẩu' }],
};
