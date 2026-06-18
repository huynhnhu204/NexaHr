import { useEffect, useState } from 'react';
import { Table, Input, Select, Drawer, Descriptions, Button } from 'antd';
import { Download } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import { SkeletonTable } from '../../components/common/Skeleton';
import EmptyState from '../../components/common/EmptyState';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { ACTION_BADGE } from '../../utils/constants';
import { formatDateTime } from '../../utils/formatDate';

const AuditLogsPage = () => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const [actionFilter, setActionFilter] = useState(null);
  const [entityFilter, setEntityFilter] = useState(null);
  const [selected, setSelected] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const fetch = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.AUDIT_LOGS.BASE, {
        params: { page: page - 1, size: 20, search, action: actionFilter, entityType: entityFilter },
      });
      setData(res.data?.content || []);
      setTotal(res.data?.totalElements || 0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetch(); }, [page, actionFilter, entityFilter]);

  const columns = [
    { title: 'Thời gian', dataIndex: 'createdAt', width: 160, render: (v) => formatDateTime(v) },
    { title: 'Người dùng', dataIndex: 'username', width: 140 },
    { title: 'Hành động', dataIndex: 'action', width: 120, render: (a) => <StatusBadge status={a} map={ACTION_BADGE} /> },
    { title: 'Đối tượng', dataIndex: 'entityType', width: 120 },
    { title: 'Chi tiết', dataIndex: 'details', ellipsis: true },
    { title: 'IP', dataIndex: 'ipAddress', width: 120 },
  ];

  const exportCsv = async () => {
    const res = await axiosClient.get(ENDPOINTS.AUDIT_LOGS.EXPORT, {
      params: { search, action: actionFilter, entityType: entityFilter },
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res]));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'audit-logs.csv';
    link.click();
    window.URL.revokeObjectURL(url);
  };

  return (
    <div>
      <PageHeader
        title="Nhật ký bảo mật"
        subtitle="Theo dõi sự kiện bảo mật và truy cập hệ thống"
        extra={<Button icon={<Download size={16} />} onClick={exportCsv}>Xuất CSV</Button>}
      />
      <div className="filter-bar">
        <Input.Search placeholder="Tìm kiếm..." style={{ width: 280 }}
          onSearch={(v) => { setSearch(v); setPage(1); fetch(); }} allowClear />
        <Select placeholder="Hành động" allowClear style={{ width: 160 }} onChange={(v) => { setActionFilter(v); setPage(1); }}
          options={Object.entries(ACTION_BADGE).map(([k, v]) => ({ value: k, label: v.label }))} />
        <Select placeholder="Đối tượng" allowClear style={{ width: 160 }} onChange={(v) => { setEntityFilter(v); setPage(1); }}
          options={['USER', 'EMPLOYEE', 'PAYROLL', 'AUTH', 'ASSET'].map((e) => ({ value: e, label: e }))} />
      </div>
      {loading ? <SkeletonTable /> : (
        <div className="data-table-card table-responsive">
          <Table
            columns={columns}
            dataSource={data}
            rowKey="id"
            onRow={(record) => ({
              onClick: () => { setSelected(record); setDrawerOpen(true); },
              style: { cursor: 'pointer' },
            })}
            pagination={{ current: page, total, pageSize: 20, onChange: setPage, showTotal: (t) => `Tổng ${t} bản ghi` }}
            locale={{ emptyText: <EmptyState title="Chưa có nhật ký bảo mật" /> }}
          />
        </div>
      )}

      <Drawer title="Chi tiết audit log" open={drawerOpen} onClose={() => setDrawerOpen(false)} width={520}>
        {selected && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="Thời gian">{formatDateTime(selected.createdAt)}</Descriptions.Item>
            <Descriptions.Item label="Người dùng">{selected.username}</Descriptions.Item>
            <Descriptions.Item label="Hành động"><StatusBadge status={selected.action} map={ACTION_BADGE} /></Descriptions.Item>
            <Descriptions.Item label="Đối tượng">{selected.entityType} #{selected.entityId}</Descriptions.Item>
            <Descriptions.Item label="IP">{selected.ipAddress}</Descriptions.Item>
            <Descriptions.Item label="Trình duyệt">{selected.browser || '-'}</Descriptions.Item>
            <Descriptions.Item label="Thiết bị">{selected.device || '-'}</Descriptions.Item>
            <Descriptions.Item label="Chi tiết">{selected.details || '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default AuditLogsPage;
