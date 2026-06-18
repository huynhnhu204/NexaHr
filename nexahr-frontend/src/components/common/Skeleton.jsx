import { Skeleton, Card } from 'antd';

export const SkeletonTable = ({ rows = 8 }) => (
  <Card className="data-table-card" styles={{ body: { padding: 0 } }}>
    <div style={{ padding: '16px 20px', borderBottom: '1px solid #E2E8F0' }}>
      <Skeleton.Input active style={{ width: 200 }} />
    </div>
    {Array.from({ length: rows }).map((_, i) => (
      <div key={i} style={{ padding: '14px 20px', borderBottom: '1px solid #F1F5F9', display: 'flex', gap: 16 }}>
        <Skeleton.Avatar active size="small" />
        <Skeleton active paragraph={{ rows: 1 }} title={false} style={{ flex: 1 }} />
      </div>
    ))}
  </Card>
);

export const SkeletonDashboard = () => (
  <div className="dashboard-page">
    <div className="skeleton-header-block" />
    <div className="dashboard-stats-grid">
      {Array.from({ length: 6 }).map((_, i) => (
        <div key={i} className="stat-card skeleton-stat-card" />
      ))}
    </div>
    <div className="dashboard-panel-grid">
      {Array.from({ length: 3 }).map((_, i) => (
        <div key={i} className="chart-card skeleton-chart-card" />
      ))}
    </div>
  </div>
);

export const PageSkeleton = () => (
  <div>
    <Skeleton active paragraph={{ rows: 1 }} style={{ marginBottom: 20 }} />
    <SkeletonTable />
  </div>
);
