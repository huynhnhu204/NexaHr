import { useEffect, useState } from 'react';
import { Card, Row, Col, Progress, Button, Tag, message, Modal, Descriptions, Table } from 'antd';
import { Check, Crown, Zap, Building2, ExternalLink, CreditCard } from 'lucide-react';
import { useSelector } from 'react-redux';
import { useSearchParams } from 'react-router-dom';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { usePermission } from '../../hooks/usePermission';
import { formatDateTime } from '../../utils/formatDate';

const PLAN_META = {
  FREE: { label: 'Miễn phí', color: '#64748B', icon: Building2 },
  PRO: { label: 'Pro', color: '#2563EB', icon: Zap },
  ENTERPRISE: { label: 'Enterprise', color: '#8B5CF6', icon: Crown },
};

const INVOICE_STATUS = {
  PENDING: { label: 'Chờ thanh toán', color: 'gold' },
  PAID: { label: 'Đã thanh toán', color: 'green' },
  FAILED: { label: 'Thất bại', color: 'red' },
  CANCELLED: { label: 'Đã hủy', color: 'default' },
};

const SubscriptionPage = () => {
  const { isAdmin } = usePermission();
  const company = useSelector((state) => state.auth.company);
  const [sub, setSub] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();

  const fetch = async () => {
    setLoading(true);
    try {
      const [subRes, invRes] = await Promise.all([
        axiosClient.get(ENDPOINTS.SUBSCRIPTION.BASE),
        isAdmin ? axiosClient.get(ENDPOINTS.BILLING.INVOICES, { params: { size: 10 } }) : Promise.resolve(null),
      ]);
      setSub(subRes.data);
      if (invRes) setInvoices(invRes.data?.content || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetch(); }, [isAdmin]);

  useEffect(() => {
    const checkoutSession = searchParams.get('checkout');
    const success = searchParams.get('success');
    const sessionId = searchParams.get('session_id');

    if (checkoutSession && isAdmin) {
      Modal.confirm({
        title: 'Xác nhận thanh toán demo',
        content: 'Đây là chế độ demo — xác nhận để nâng cấp gói ngay lập tức.',
        okText: 'Xác nhận thanh toán',
        cancelText: 'Hủy',
        onOk: async () => {
          await axiosClient.post(ENDPOINTS.BILLING.CONFIRM, null, { params: { sessionId: checkoutSession } });
          message.success('Thanh toán thành công — gói đã được nâng cấp');
          setSearchParams({});
          fetch();
        },
        onCancel: () => setSearchParams({}),
      });
    } else if (success === 'true' && sessionId) {
      message.success('Thanh toán Stripe thành công');
      setSearchParams({});
      fetch();
    }
  }, [searchParams, isAdmin, setSearchParams]);

  const handleUpgrade = async (plan) => {
    if (plan === 'FREE') {
      Modal.confirm({
        title: 'Hạ cấp về gói miễn phí?',
        content: 'Gói sẽ được cập nhật ngay lập tức (chế độ demo).',
        okText: 'Xác nhận',
        cancelText: 'Hủy',
        onOk: async () => {
          await axiosClient.put(ENDPOINTS.SUBSCRIPTION.UPGRADE, { plan });
          message.success('Cập nhật gói thành công');
          fetch();
        },
      });
      return;
    }

    Modal.confirm({
      title: `Nâng cấp lên gói ${PLAN_META[plan]?.label || plan}?`,
      content: 'Bạn sẽ được chuyển đến trang thanh toán (Stripe hoặc chế độ demo).',
      okText: 'Tiếp tục thanh toán',
      cancelText: 'Hủy',
      onOk: async () => {
        setCheckoutLoading(true);
        try {
          const res = await axiosClient.post(ENDPOINTS.BILLING.CHECKOUT, { plan });
          const checkout = res.data;
          if (checkout.mockMode && checkout.checkoutUrl) {
            window.location.href = checkout.checkoutUrl;
          } else if (checkout.checkoutUrl) {
            window.location.href = checkout.checkoutUrl;
          } else {
            message.error('Không thể tạo phiên thanh toán');
          }
        } finally {
          setCheckoutLoading(false);
        }
      },
    });
  };

  const plan = sub?.plan || 'FREE';
  const meta = PLAN_META[plan] || PLAN_META.FREE;
  const Icon = meta.icon;
  const usage = sub?.usagePercent || 0;

  const invoiceColumns = [
    { title: 'Mã hóa đơn', dataIndex: 'invoiceNumber', key: 'invoiceNumber' },
    { title: 'Gói', dataIndex: 'plan', key: 'plan', render: (p) => PLAN_META[p]?.label || p },
    { title: 'Số tiền', dataIndex: 'amount', key: 'amount', render: (v) => `${Number(v).toLocaleString('vi-VN')}đ` },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status', render: (s) => <Tag color={INVOICE_STATUS[s]?.color}>{INVOICE_STATUS[s]?.label || s}</Tag> },
    { title: 'Ngày tạo', dataIndex: 'createdAt', key: 'createdAt', render: (v) => formatDateTime(v) },
  ];

  return (
    <div>
      <PageHeader
        title="Gói đăng ký & Thanh toán"
        subtitle="Quản lý gói SaaS, thanh toán Stripe và lịch sử hóa đơn"
        extra={
          company?.code && (
            <Button icon={<ExternalLink size={16} />} href={`/careers/${company.code}`} target="_blank">
              Xem trang tuyển dụng
            </Button>
          )
        }
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card loading={loading} className="subscription-current-card">
            <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
              <div className="plan-icon" style={{ background: `${meta.color}15` }}>
                <Icon size={28} color={meta.color} />
              </div>
              <div>
                <Tag color={meta.color}>{meta.label}</Tag>
                <h3 style={{ margin: '8px 0 0', fontSize: 22 }}>{sub?.price ? `${sub.price.toLocaleString('vi-VN')}đ/tháng` : 'Miễn phí'}</h3>
              </div>
            </div>

            <div style={{ marginBottom: 20 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                <span>Nhân viên</span>
                <span>{sub?.currentEmployees || 0} / {sub?.maxEmployees === -1 ? '∞' : sub?.maxEmployees || '∞'}</span>
              </div>
              <Progress percent={usage} status={usage > 90 ? 'exception' : 'active'} strokeColor={meta.color} />
            </div>

            <Descriptions column={1} size="small">
              <Descriptions.Item label="Email thanh toán">{sub?.billingEmail || '—'}</Descriptions.Item>
              <Descriptions.Item label="Gia hạn tiếp theo">{sub?.nextBillingDate || 'Không áp dụng (gói miễn phí)'}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>

        <Col xs={24} lg={10}>
          <Card title="Tính năng gói hiện tại" loading={loading}>
            <ul className="plan-features">
              {(sub?.features || []).map((f) => (
                <li key={f}><Check size={16} color="#22C55E" /> {f}</li>
              ))}
            </ul>
          </Card>
        </Col>
      </Row>

      {isAdmin && (
        <div style={{ marginTop: 24 }}>
          <h4 style={{ marginBottom: 16 }}>Nâng cấp gói</h4>
          <Row gutter={[16, 16]}>
            {['FREE', 'PRO', 'ENTERPRISE'].map((p) => {
              const m = PLAN_META[p];
              const PIcon = m.icon;
              const isCurrent = plan === p;
              return (
                <Col xs={24} md={8} key={p}>
                  <Card className={`plan-card ${isCurrent ? 'plan-card-active' : ''}`}>
                    <PIcon size={24} color={m.color} />
                    <h4 style={{ margin: '12px 0 4px' }}>{m.label}</h4>
                    <p style={{ color: '#64748B', fontSize: 13, marginBottom: 16 }}>
                      {p === 'FREE' && 'Tối đa 10 nhân viên'}
                      {p === 'PRO' && 'Tối đa 100 nhân viên — 999.000đ/tháng'}
                      {p === 'ENTERPRISE' && 'Không giới hạn — 4.999.000đ/tháng'}
                    </p>
                    <Button
                      type={isCurrent ? 'default' : 'primary'}
                      block
                      disabled={isCurrent}
                      loading={checkoutLoading && !isCurrent}
                      icon={!isCurrent && p !== 'FREE' ? <CreditCard size={16} /> : null}
                      onClick={() => handleUpgrade(p)}
                    >
                      {isCurrent ? 'Gói hiện tại' : p === 'FREE' ? 'Chọn gói' : 'Thanh toán & nâng cấp'}
                    </Button>
                  </Card>
                </Col>
              );
            })}
          </Row>
        </div>
      )}

      {isAdmin && (
        <Card title="Lịch sử thanh toán" style={{ marginTop: 24 }} loading={loading}>
          <Table
            rowKey="id"
            columns={invoiceColumns}
            dataSource={invoices}
            pagination={false}
            locale={{ emptyText: 'Chưa có hóa đơn' }}
            scroll={{ x: 600 }}
          />
        </Card>
      )}
    </div>
  );
};

export default SubscriptionPage;
