import { useEffect, useState } from 'react';
import { Table, Button, DatePicker, Select, message, Statistic, Row, Col, Modal, Space } from 'antd';
import { RefreshCw, FileSpreadsheet, FileText, CheckCircle, DollarSign, Eye } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import { PAYROLL_STATUS } from '../../utils/constants';
import { formatCurrency } from '../../utils/formatCurrency';
import { usePermission } from '../../hooks/usePermission';
import usePayroll from './hooks/usePayroll';

const PayrollPage = () => {
  const navigate = useNavigate();
  const { canViewAllPayroll } = usePermission();
  const {
    payrolls, loading, total, page, setPage, filters, setFilters,
    fetchPayrolls, generatePayroll, approvePayroll, markPaid, exportFile,
  } = usePayroll();
  const [generating, setGenerating] = useState(false);
  const [month, setMonth] = useState(dayjs());

  useEffect(() => { fetchPayrolls(canViewAllPayroll); }, [page, filters, canViewAllPayroll]);

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      await generatePayroll(month.format('YYYY-MM'));
      message.success('Tạo bảng lương thành công');
      fetchPayrolls(canViewAllPayroll);
    } catch (err) {
      message.error(err.message);
    } finally {
      setGenerating(false);
    }
  };

  const handleApprove = (record) => {
    Modal.confirm({
      title: 'Phê duyệt bảng lương',
      content: `Xác nhận phê duyệt bảng lương tháng ${record.salaryMonth} của ${record.employeeName}?`,
      okText: 'Phê duyệt',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await approvePayroll(record.id);
          message.success('Đã phê duyệt bảng lương');
          fetchPayrolls(canViewAllPayroll);
        } catch (err) {
          message.error(err.message);
        }
      },
    });
  };

  const handleMarkPaid = (record) => {
    Modal.confirm({
      title: 'Đánh dấu đã thanh toán',
      content: `Xác nhận đã thanh toán lương cho ${record.employeeName}?`,
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await markPaid(record.id);
          message.success('Đã đánh dấu thanh toán');
          fetchPayrolls(canViewAllPayroll);
        } catch (err) {
          message.error(err.message);
        }
      },
    });
  };

  const handleExport = async (id, type) => {
    try {
      await exportFile(id, type);
      message.success(`Xuất ${type === 'pdf' ? 'PDF' : 'Excel'} thành công`);
    } catch (err) {
      message.error(err.message || 'Xuất file thất bại');
    }
  };

  const totalPayroll = payrolls.reduce((sum, p) => sum + (Number(p.netSalary) || 0), 0);

  const columns = [
    ...(canViewAllPayroll ? [
      { title: 'Nhân viên', dataIndex: 'employeeName' },
      { title: 'Mã NV', dataIndex: 'employeeCode' },
      { title: 'Phòng ban', dataIndex: 'departmentName' },
    ] : []),
    { title: 'Tháng', dataIndex: 'salaryMonth' },
    { title: 'Lương cơ bản', dataIndex: 'baseSalary', render: (v) => formatCurrency(v) },
    { title: 'Phụ cấp', dataIndex: 'allowance', render: (v) => formatCurrency(v) },
    { title: 'Thưởng', dataIndex: 'bonus', render: (v) => formatCurrency(v) },
    { title: 'Khấu trừ', dataIndex: 'deduction', render: (v) => formatCurrency(v) },
    { title: 'Lương thực nhận', dataIndex: 'netSalary', render: (v) => <strong>{formatCurrency(v)}</strong> },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => <StatusBadge status={s} map={PAYROLL_STATUS} /> },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 200,
      render: (_, record) => (
        <Space size="small" wrap>
          <Button type="text" icon={<Eye size={16} />} onClick={() => navigate(`/payroll/${record.id}`)} />
          {canViewAllPayroll && record.status === 'DRAFT' && (
            <Button type="text" icon={<CheckCircle size={16} />} onClick={() => handleApprove(record)} title="Phê duyệt" />
          )}
          {canViewAllPayroll && record.status === 'APPROVED' && (
            <Button type="text" icon={<DollarSign size={16} />} onClick={() => handleMarkPaid(record)} title="Đã thanh toán" />
          )}
          {canViewAllPayroll && (
            <>
              <Button type="text" icon={<FileSpreadsheet size={16} />} onClick={() => handleExport(record.id, 'excel')} title="Xuất Excel" />
              <Button type="text" icon={<FileText size={16} />} onClick={() => handleExport(record.id, 'pdf')} title="Xuất PDF" />
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Bảng lương"
        subtitle="Quản lý lương thưởng nhân viên"
        extra={canViewAllPayroll && (
          <Button type="primary" icon={<RefreshCw size={16} />} loading={generating} onClick={handleGenerate}
            style={{ background: '#1E3A8A' }}>
            Tạo bảng lương
          </Button>
        )}
      />

      {canViewAllPayroll && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col>
            <div className="stat-card" style={{ display: 'inline-block', minWidth: 200 }}>
              <Statistic title="Tổng chi lương" value={totalPayroll} formatter={(v) => formatCurrency(v)} />
            </div>
          </Col>
        </Row>
      )}

      {canViewAllPayroll && (
        <div className="filter-bar">
          <DatePicker
            picker="month"
            value={month}
            onChange={(v) => {
              setMonth(v);
              setFilters({ ...filters, month: v ? v.format('YYYY-MM') : null });
              setPage(0);
            }}
          />
          <Select
            placeholder="Trạng thái"
            allowClear
            style={{ width: 160 }}
            onChange={(v) => { setFilters({ ...filters, status: v }); setPage(0); }}
            options={Object.entries(PAYROLL_STATUS).map(([k, val]) => ({ value: k, label: val.label }))}
          />
        </div>
      )}

      <div className="data-table-card table-responsive">
        <Table columns={columns} dataSource={payrolls} rowKey="id" loading={loading}
          pagination={{ current: page + 1, total, pageSize: 10, onChange: (p) => setPage(p - 1) }} />
      </div>
    </div>
  );
};

export default PayrollPage;
