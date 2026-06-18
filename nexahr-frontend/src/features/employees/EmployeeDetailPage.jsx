import { useParams } from 'react-router-dom';
import { Avatar, Tabs, Descriptions, Table, Row, Col, Statistic } from 'antd';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import { PageSkeleton } from '../../components/common/Skeleton';
import EmployeeDocuments from './components/EmployeeDocuments';
import EmployeeTimeline from './components/EmployeeTimeline';
import useEmployeeProfile from './hooks/useEmployeeProfile';
import {
  EMPLOYMENT_STATUS, ATTENDANCE_STATUS, PAYROLL_STATUS,
} from '../../utils/constants';
import { formatDate } from '../../utils/formatDate';
import { formatCurrency } from '../../utils/formatCurrency';

const genderLabel = (g) => {
  if (g === 'Male') return 'Nam';
  if (g === 'Female') return 'Nữ';
  if (g === 'Other') return 'Khác';
  return g || '-';
};

const EmployeeDetailPage = () => {
  const { id } = useParams();
  const {
    employee, documents, timeline, attendance, leaves, payrolls,
    loading, fetchProfile, setDocuments,
  } = useEmployeeProfile(id);

  if (loading) return <PageSkeleton />;
  if (!employee) return null;

  const tabItems = [
    {
      key: 'overview',
      label: 'Tổng quan',
      children: (
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={8}>
            <div className="stat-card">
              <Statistic title="Ngày phép còn lại" value={employee.annualLeaveBalance || 0} suffix="ngày" />
            </div>
          </Col>
          <Col xs={24} sm={8}>
            <div className="stat-card">
              <Statistic title="Chấm công gần đây" value={attendance.length} suffix="bản ghi" />
            </div>
          </Col>
          <Col xs={24} sm={8}>
            <div className="stat-card">
              <Statistic title="Tài liệu" value={documents.length} suffix="tệp" />
            </div>
          </Col>
          <Col xs={24} lg={12}>
            <div className="chart-card">
              <h4>Chấm công gần đây</h4>
              <Table size="small" dataSource={attendance} rowKey="id" pagination={false}
                columns={[
                  { title: 'Ngày', dataIndex: 'workDate', render: (v) => formatDate(v) },
                  { title: 'Giờ vào', dataIndex: 'checkInTime', render: (v) => (v ? formatDate(v, 'HH:mm') : '-') },
                  { title: 'Trạng thái', dataIndex: 'status', render: (s) => <StatusBadge status={s} map={ATTENDANCE_STATUS} /> },
                ]} />
            </div>
          </Col>
          <Col xs={24} lg={12}>
            <div className="chart-card">
              <h4>Bảng lương gần đây</h4>
              <Table size="small" dataSource={payrolls} rowKey="id" pagination={false}
                columns={[
                  { title: 'Tháng', dataIndex: 'salaryMonth' },
                  { title: 'Thực nhận', dataIndex: 'netSalary', render: (v) => formatCurrency(v) },
                  { title: 'Trạng thái', dataIndex: 'status', render: (s) => <StatusBadge status={s} map={PAYROLL_STATUS} /> },
                ]} />
            </div>
          </Col>
        </Row>
      ),
    },
    {
      key: 'personal',
      label: 'Thông tin cá nhân',
      children: (
        <div className="chart-card">
          <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
            <Descriptions.Item label="Mã nhân viên">{employee.employeeCode}</Descriptions.Item>
            <Descriptions.Item label="Email">{employee.email}</Descriptions.Item>
            <Descriptions.Item label="Email cá nhân">{employee.personalEmail || '-'}</Descriptions.Item>
            <Descriptions.Item label="Số điện thoại">{employee.phone || '-'}</Descriptions.Item>
            <Descriptions.Item label="Giới tính">{genderLabel(employee.gender)}</Descriptions.Item>
            <Descriptions.Item label="Ngày sinh">{formatDate(employee.dateOfBirth)}</Descriptions.Item>
            <Descriptions.Item label="CMND/CCCD">{employee.nationalId || '-'}</Descriptions.Item>
            <Descriptions.Item label="Địa chỉ" span={2}>{employee.address || '-'}</Descriptions.Item>
          </Descriptions>
        </div>
      ),
    },
    {
      key: 'work',
      label: 'Công việc',
      children: (
        <div className="chart-card">
          <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
            <Descriptions.Item label="Phòng ban">{employee.departmentName || '-'}</Descriptions.Item>
            <Descriptions.Item label="Chức vụ">{employee.positionName || '-'}</Descriptions.Item>
            <Descriptions.Item label="Quản lý trực tiếp">{employee.managerName || '-'}</Descriptions.Item>
            <Descriptions.Item label="Ngày vào làm">{formatDate(employee.hireDate)}</Descriptions.Item>
            <Descriptions.Item label="Trạng thái">
              <StatusBadge status={employee.employmentStatus} map={EMPLOYMENT_STATUS} />
            </Descriptions.Item>
            <Descriptions.Item label="Ngày phép còn lại">{employee.annualLeaveBalance} ngày</Descriptions.Item>
          </Descriptions>
        </div>
      ),
    },
    {
      key: 'contract',
      label: 'Hợp đồng',
      children: (
        <div className="chart-card">
          <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
            <Descriptions.Item label="Loại hợp đồng">{employee.contractType || '-'}</Descriptions.Item>
            <Descriptions.Item label="Ngày bắt đầu">{formatDate(employee.contractStartDate)}</Descriptions.Item>
            <Descriptions.Item label="Ngày kết thúc">{formatDate(employee.contractEndDate)}</Descriptions.Item>
            <Descriptions.Item label="Ngày vào làm">{formatDate(employee.hireDate)}</Descriptions.Item>
          </Descriptions>
        </div>
      ),
    },
    {
      key: 'emergency',
      label: 'Liên hệ khẩn cấp',
      children: (
        <div className="chart-card">
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="Số điện thoại liên hệ">{employee.phone || 'Chưa cập nhật'}</Descriptions.Item>
            <Descriptions.Item label="Email liên hệ">{employee.personalEmail || employee.email}</Descriptions.Item>
            <Descriptions.Item label="Địa chỉ liên hệ">{employee.address || 'Chưa cập nhật'}</Descriptions.Item>
          </Descriptions>
        </div>
      ),
    },
    {
      key: 'documents',
      label: 'Tài liệu',
      children: (
        <div className="chart-card">
          <EmployeeDocuments
            employeeId={id}
            documents={documents}
            onRefresh={fetchProfile}
            onDocumentsChange={setDocuments}
          />
        </div>
      ),
    },
    {
      key: 'timeline',
      label: 'Timeline',
      children: (
        <div className="chart-card">
          <EmployeeTimeline events={timeline} />
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title={employee.fullName}
        subtitle={`${employee.positionName || ''} · ${employee.departmentName || ''}`}
        breadcrumb={[
          { title: 'Nhân sự' },
          { title: 'Nhân viên' },
          { title: employee.fullName },
        ]}
      />

      <div className="profile-header">
        <Avatar size={80} style={{ backgroundColor: '#2563eb', fontSize: 32 }}>{employee.fullName?.charAt(0)}</Avatar>
        <div>
          <h2 style={{ fontSize: 24, fontWeight: 700 }}>{employee.fullName}</h2>
          <p style={{ color: '#64748b' }}>{employee.employeeCode} · {employee.email}</p>
          <StatusBadge status={employee.employmentStatus} map={EMPLOYMENT_STATUS} />
        </div>
      </div>

      <Tabs items={tabItems} defaultActiveKey="overview" />
    </div>
  );
};

export default EmployeeDetailPage;
