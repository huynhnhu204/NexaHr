import { Tag } from 'antd';

const StatusBadge = ({ status, map }) => {
  const cfg = map?.[status] || { label: status, color: 'default' };
  return <Tag color={cfg.color} className="status-badge">{cfg.label}</Tag>;
};

export default StatusBadge;
