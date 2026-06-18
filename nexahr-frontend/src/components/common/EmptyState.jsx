import { Empty } from 'antd';
import { Inbox } from 'lucide-react';

const EmptyState = ({
  title = 'Chưa có dữ liệu',
  description = 'Dữ liệu sẽ hiển thị tại đây khi có bản ghi mới.',
  action,
}) => (
  <div className="empty-state">
    <Empty
      image={<Inbox size={48} strokeWidth={1.2} color="#94A3B8" />}
      description={
        <div>
          <div className="empty-state-title">{title}</div>
          <div className="empty-state-desc">{description}</div>
        </div>
      }
    >
      {action}
    </Empty>
  </div>
);

export default EmptyState;
