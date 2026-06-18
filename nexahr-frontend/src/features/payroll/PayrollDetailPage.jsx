import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Descriptions, Divider, Row, Col, Space, Modal, message } from 'antd';
import { ArrowLeft, FileSpreadsheet, FileText, CheckCircle, DollarSign } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import { PageSkeleton } from '../../components/common/Skeleton';
import { PAYROLL_STATUS } from '../../utils/constants';
import { formatCurrency } from '../../utils/formatCurrency';
import { usePermission } from '../../hooks/usePermission';
import usePayroll from './hooks/usePayroll';

const PayrollDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { canViewAllPayroll } = usePermission();
  const { payroll, loading, fetchPayrollById, approvePayroll, markPaid, exportFile } = usePayroll();

  useEffect(() => { fetchPayrollById(id); }, [id]);

  if (loading || !payroll) return <PageSkeleton />;

  const handleApprove = () => {
    Modal.confirm({
      title: 'Phê duyệt bảng lương',
      content: `Xác nhận phê duyệt bảng lương tháng ${payroll.salaryMonth}?`,
      okText: 'Phê duyệt',
      onOk: async () => {
        await approvePayroll(id);
        message.success('Đã phê duyệt');
        fetchPayrollById(id);
      },
    });
  };

  const handleMarkPaid = () => {
    Modal.confirm({
      title: 'Đánh dấu đã thanh toán',
      okText: 'Xác nhận',
      onOk: async () => {
        await markPaid(id);
        message.success('Đã đánh dấu thanh toán');
        fetchPayrollById(id);
      },
    });
  };

  return (
    <div>
      <PageHeader
        title="Phiếu lương"
        subtitle={`${payroll.employeeName} · Tháng ${payroll.salaryMonth}`}
        breadcrumb={[{ title: 'Bảng lương' }, { title: `Phiếu lương #${id}` }]}
        extra={(
          <Space>
            <Button icon={<ArrowLeft size={16} />} onClick={() => navigate('/payroll')}>Quay lại</Button>
            {canViewAllPayroll && (
              <>
                <Button icon={<FileSpreadsheet size={16} />} onClick={() => exportFile(id, 'excel')}>Excel</Button>
                <Button icon={<FileText size={16} />} onClick={() => exportFile(id, 'pdf')}>PDF</Button>
                {payroll.status === 'DRAFT' && (
                  <Button type="primary" icon={<CheckCircle size={16} />} onClick={handleApprove} style={{ background: '#1E3A8A' }}>
                    Phê duyệt
                  </Button>
                )}
                {payroll.status === 'APPROVED' && (
                  <Button type="primary" icon={<DollarSign size={16} />} onClick={handleMarkPaid} style={{ background: '#16A34A' }}>
                    Đã thanh toán
                  </Button>
                )}
              </>
            )}
          </Space>
        )}
      />

      <div className="chart-card payslip-card">
        <div className="payslip-header">
          <div>
            <h2 style={{ margin: 0, color: '#1E3A8A' }}>NexaHR</h2>
            <p style={{ color: '#64748B', margin: 0 }}>Phiếu lương tháng {payroll.salaryMonth}</p>
          </div>
          <StatusBadge status={payroll.status} map={PAYROLL_STATUS} />
        </div>

        <Divider />

        <Descriptions column={{ xs: 1, sm: 2 }} size="small">
          <Descriptions.Item label="Nhân viên">{payroll.employeeName}</Descriptions.Item>
          <Descriptions.Item label="Mã NV">{payroll.employeeCode}</Descriptions.Item>
          <Descriptions.Item label="Phòng ban">{payroll.departmentName}</Descriptions.Item>
          <Descriptions.Item label="Tháng lương">{payroll.salaryMonth}</Descriptions.Item>
        </Descriptions>

        <Divider orientation="left">Thu nhập</Divider>
        <Row gutter={[16, 8]}>
          <Col span={12}>Lương cơ bản</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.baseSalary)}</Col>
          <Col span={12}>Phụ cấp</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.allowance)}</Col>
          <Col span={12}>Thưởng</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.bonus)}</Col>
          <Col span={12}>Làm thêm giờ ({payroll.overtimeHours || 0}h)</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.overtimePay)}</Col>
          <Col span={12}><strong>Tổng thu nhập</strong></Col>
          <Col span={12} style={{ textAlign: 'right' }}><strong>{formatCurrency(payroll.grossIncome)}</strong></Col>
        </Row>

        <Divider orientation="left">Khấu trừ</Divider>
        <Row gutter={[16, 8]}>
          <Col span={12}>BHXH</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.socialInsurance)}</Col>
          <Col span={12}>BHYT</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.healthInsurance)}</Col>
          <Col span={12}>BHTN</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.unemploymentInsurance)}</Col>
          <Col span={12}>Thuế TNCN</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.personalIncomeTax)}</Col>
          <Col span={12}>Khấu trừ khác</Col>
          <Col span={12} style={{ textAlign: 'right' }}>{formatCurrency(payroll.deduction)}</Col>
          <Col span={12}><strong>Tổng khấu trừ</strong></Col>
          <Col span={12} style={{ textAlign: 'right' }}><strong>{formatCurrency(payroll.totalDeduction || payroll.deduction)}</strong></Col>
        </Row>

        <Divider />
        <div className="payslip-net">
          <span>Lương thực nhận</span>
          <strong>{formatCurrency(payroll.netSalary)}</strong>
        </div>

        <div style={{ marginTop: 16, color: '#64748B', fontSize: 13 }}>
          Ngày công: {payroll.actualWorkingDays || payroll.workingDays}/{payroll.standardWorkingDays || 22}
        </div>
      </div>
    </div>
  );
};

export default PayrollDetailPage;
