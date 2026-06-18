import { useEffect, useState } from 'react';
import { Table, Input, Select, DatePicker, Drawer, Descriptions } from 'antd';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import { SkeletonTable } from '../../components/common/Skeleton';
import EmptyState from '../../components/common/EmptyState';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { ACTION_BADGE } from '../../utils/constants';
import { formatDateTime } from '../../utils/formatDate';

const { RangePicker } = DatePicker;

const ActivityLogsPage = () => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const [actionFilter, setActionFilter] = useState(null);
  const [dateRange, setDateRange] = useState(null);
  const [selected, setSelected] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const fetch = async () => {
    setLoading(true);
    try {
      const params = { page: page - 1, size: 20, search, action: actionFilter };
      if (dateRange?.[0]) params.from = dateRange[0].format('YYYY-MM-DD');
      if (dateRange?.[1]) params.to = dateRange[1].format('YYYY-MM-DD');
      const res = await axiosClient.get(ENDPOINTS.ACTIVITY_LOGS, { params });
      setData(res.data?.content || []);
      setTotal(res.data?.totalElements || 0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetch(); }, [page, actionFilter, dateRange]);

  const columns = [
    { title: 'Thời gian', dataIndex: 'createdAt', width: 160, render: (v) => formatDateTime(v) },
    { title: 'Người dùng', dataIndex: 'username', width: 140 },
    { title: 'Hành động', dataIndex: 'action', width: 120, render: (a) => <StatusBadge status={a} map={ACTION_BADGE} /> },
    { title: 'Module', dataIndex: 'module', width: 120 },
    { title: 'Mô tả', dataIndex: 'description', ellipsis: true },
    { title: 'IP', dataIndex: 'ipAddress', width: 120 },
  ];

  return (
    <div>
      <PageHeader title="Nhật ký hoạt động" subtitle="Theo dõi thao tác người dùng trên hệ thống" />
      <div className="filter-bar">
        <Input.Search placeholder="Tìm theo người dùng, mô tả..." style={{ width: 280 }}
          onSearch={(v) => { setSearch(v); setPage(1); fetch(); }} allowClear />
        <RangePicker value={dateRange} onChange={(v) => { setDateRange(v); setPage(1); }} format="DD/MM/YYYY" />
        <Select placeholder="Hành động" allowClear style={{ width: 160 }} onChange={(v) => { setActionFilter(v); setPage(1); }}
          options={Object.entries(ACTION_BADGE).map(([k, v]) => ({ value: k, label: v.label }))} />
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
            locale={{ emptyText: <EmptyState title="Chưa có nhật ký" /> }}
          />
        </div>
      )}

      <Drawer title="Chi tiết nhật ký" open={drawerOpen} onClose={() => setDrawerOpen(false)} width={480}>
        {selected && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="Thời gian">{formatDateTime(selected.createdAt)}</Descriptions.Item>
            <Descriptions.Item label="Người dùng">{selected.username}</Descriptions.Item>
            <Descriptions.Item label="Hành động"><StatusBadge status={selected.action} map={ACTION_BADGE} /></Descriptions.Item>
            <Descriptions.Item label="Module">{selected.module}</Descriptions.Item>
            <Descriptions.Item label="Mô tả">{selected.description}</Descriptions.Item>
            <Descriptions.Item label="IP">{selected.ipAddress}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default ActivityLogsPage;
