import { Timeline } from 'antd';
import { formatDateTime } from '../../../utils/formatDate';
import EmptyState from '../../../components/common/EmptyState';

const TYPE_COLORS = {
  HIRE: 'green',
  PROMOTION: 'blue',
  TRANSFER: 'orange',
  CONTRACT: 'purple',
  LEAVE: 'gold',
  DOCUMENT: 'cyan',
  DEFAULT: 'gray',
};

const EmployeeTimeline = ({ events }) => {
  if (!events?.length) {
    return <EmptyState title="Chưa có sự kiện" description="Lịch sử hoạt động sẽ hiển thị tại đây." />;
  }

  return (
    <Timeline
      items={events.map((event) => ({
        color: TYPE_COLORS[event.type] || TYPE_COLORS.DEFAULT,
        children: (
          <div>
            <div style={{ fontWeight: 600, color: '#0F172A' }}>{event.title}</div>
            {event.description && <div style={{ color: '#64748B', fontSize: 13, marginTop: 2 }}>{event.description}</div>}
            <div style={{ color: '#94A3B8', fontSize: 12, marginTop: 4 }}>{formatDateTime(event.occurredAt)}</div>
          </div>
        ),
      }))}
    />
  );
};

export default EmployeeTimeline;
