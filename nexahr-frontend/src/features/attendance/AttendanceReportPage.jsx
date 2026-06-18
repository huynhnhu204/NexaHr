import { Table, DatePicker, Select, Button } from 'antd';
import PageHeader from '../../components/common/PageHeader';
import { SkeletonTable } from '../../components/common/Skeleton';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import { ATTENDANCE_STATUS } from '../../utils/constants';
import { useAttendanceReport } from './hooks/useAttendanceReport';
import { formatDate } from '../../utils/formatDate';

const { RangePicker } = DatePicker;

const AttendanceReportPage = () => {
  const { data, loading, filters, setFilters, fetchReport } = useAttendanceReport();

  const columns = [
    { title: 'Nhân viên', dataIndex: 'employeeName', key: 'employeeName' },
    { title: 'Mã NV', dataIndex: 'employeeCode', key: 'employeeCode' },
    { title: 'Ngày', dataIndex: 'date', render: (v) => formatDate(v) },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => <StatusBadge status={s} map={ATTENDANCE_STATUS} /> },
    { title: 'Giờ làm', dataIndex: 'workHours', render: (v) => v ? `${v}h` : '-' },
  ];

  return (
    <div>
      <PageHeader title="Báo cáo chấm công" subtitle="Thống kê chấm công theo khoảng thời gian" />
      <div className="filter-bar">
        <RangePicker onChange={(dates) => setFilters({ ...filters, from: dates?.[0], to: dates?.[1] })} />
        <Select
          placeholder="Trạng thái"
          allowClear
          style={{ width: 160 }}
          onChange={(v) => setFilters({ ...filters, status: v })}
          options={Object.entries(ATTENDANCE_STATUS).map(([k, v]) => ({ value: k, label: v.label }))}
        />
        <Button type="primary" onClick={fetchReport}>Lọc</Button>
      </div>
      {loading ? <SkeletonTable /> : (
        <div className="data-table-card">
          <Table
            columns={columns}
            dataSource={data}
            rowKey="id"
            pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `Tổng ${t} bản ghi` }}
            locale={{ emptyText: <EmptyState title="Không có dữ liệu chấm công" /> }}
          />
        </div>
      )}
    </div>
  );
};

export default AttendanceReportPage;
