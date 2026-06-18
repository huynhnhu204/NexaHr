import { useCallback, useEffect, useState } from 'react';
import { Table, Button, DatePicker, message, Tag, Alert, Typography } from 'antd';
import { LogIn, LogOut, MapPin, ExternalLink } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { ATTENDANCE_STATUS } from '../../utils/constants';
import { formatDate, formatDateTime } from '../../utils/formatDate';
import { formatCoords, formatDistance, googleMapsUrl } from '../../utils/geoUtils';
import { usePermission } from '../../hooks/usePermission';
import PageHeader from '../../components/common/PageHeader';
import AuthImage from '../../components/common/AuthImage';
import AttendanceCheckModal from './AttendanceCheckModal';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;
const { Link } = Typography;

const todayStatusLabel = (record) => {
  if (!record?.checkInTime) return 'Chưa check-in';
  if (!record?.checkOutTime) return 'Đã check-in';
  return 'Đã check-out';
};

const LocationInfo = ({ lat, lon, address, distance }) => {
  if (lat == null && !address) return <span>—</span>;
  return (
    <div className="attendance-location-cell">
      {address && <div>{address}</div>}
      {lat != null && (
        <>
          <div className="attendance-coords">{formatCoords(lat, lon)}</div>
          {distance != null && <div className="attendance-distance">Cách công ty: {formatDistance(distance)}</div>}
          <Link href={googleMapsUrl(lat, lon)} target="_blank" rel="noreferrer" className="attendance-map-link">
            <ExternalLink size={12} /> Mở trên Google Maps
          </Link>
        </>
      )}
    </div>
  );
};

const AttendancePage = () => {
  const { hasRole } = usePermission();
  const canViewAll = hasRole('ADMIN', 'HR', 'MANAGER');
  const [records, setRecords] = useState([]);
  const [todayRecord, setTodayRecord] = useState(null);
  const [companyLocation, setCompanyLocation] = useState(null);
  const [locationError, setLocationError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [modalMode, setModalMode] = useState(null);
  const [dateRange, setDateRange] = useState(null);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);

  const loadCompanyLocation = useCallback(async () => {
    try {
      const res = await axiosClient.get(ENDPOINTS.ATTENDANCE.COMPANY_LOCATION);
      const data = res?.data ?? res;
      setCompanyLocation(data);
      setLocationError(data?.configured ? null : 'Chưa cấu hình vị trí công ty. Vui lòng liên hệ quản trị viên.');
    } catch (err) {
      setCompanyLocation({ configured: false });
      setLocationError(err.message || 'Không tải được vị trí công ty. Hãy restart backend và thử lại.');
    }
  }, []);

  const loadToday = useCallback(async () => {
    try {
      const res = await axiosClient.get(ENDPOINTS.ATTENDANCE.TODAY);
      setTodayRecord(res?.data ?? null);
    } catch {
      setTodayRecord(null);
    }
  }, []);

  const fetchRecords = useCallback(async () => {
    setLoading(true);
    try {
      const endpoint = canViewAll ? ENDPOINTS.ATTENDANCE.BASE : ENDPOINTS.ATTENDANCE.MY;
      const params = {
        page,
        size: 10,
        startDate: dateRange?.[0]?.format('YYYY-MM-DD'),
        endDate: dateRange?.[1]?.format('YYYY-MM-DD'),
      };
      const res = await axiosClient.get(endpoint, { params });
      const pageData = res?.data ?? res;
      setRecords(pageData?.content || []);
      setTotal(pageData?.totalElements || 0);
    } catch (err) {
      message.error(err.message || 'Không tải được dữ liệu chấm công');
      setRecords([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, [canViewAll, page, dateRange]);

  useEffect(() => {
    loadCompanyLocation();
    loadToday();
    fetchRecords();
  }, [loadCompanyLocation, loadToday, fetchRecords]);

  const handleSuccess = (data) => {
    setTodayRecord(data);
    fetchRecords();
  };

  const hasCheckedIn = !!todayRecord?.checkInTime;
  const hasCheckedOut = !!todayRecord?.checkOutTime;
  const todayStatus = todayRecord?.status ? ATTENDANCE_STATUS[todayRecord.status] : null;
  const locationReady = !!companyLocation?.configured;

  const checkInDisabledReason = () => {
    if (hasCheckedIn) return 'Bạn đã check-in hôm nay.';
    if (!locationReady) return locationError || 'Chưa cấu hình vị trí công ty.';
    return null;
  };

  const checkOutDisabledReason = () => {
    if (!hasCheckedIn) return 'Cần check-in trước khi check-out.';
    if (hasCheckedOut) return 'Bạn đã check-out hôm nay.';
    if (!locationReady) return locationError || 'Chưa cấu hình vị trí công ty.';
    return null;
  };

  const columns = [
    { title: 'Nhân viên', dataIndex: 'employeeName', key: 'name' },
    { title: 'Ngày', dataIndex: 'workDate', render: (v) => formatDate(v) },
    { title: 'Giờ vào', dataIndex: 'checkInTime', render: (v) => (v ? formatDateTime(v) : '-') },
    { title: 'Giờ ra', dataIndex: 'checkOutTime', render: (v) => (v ? formatDateTime(v) : '-') },
    { title: 'Số giờ', dataIndex: 'totalHours', render: (v) => (v != null ? `${v}h` : '-') },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      render: (s) => {
        const cfg = ATTENDANCE_STATUS[s] || { label: s, color: 'default' };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    ...(canViewAll ? [
      {
        title: 'Vị trí vào',
        key: 'checkInLoc',
        render: (_, r) => (
          <LocationInfo
            lat={r.checkInLatitude}
            lon={r.checkInLongitude}
            address={r.checkInAddress}
            distance={r.checkInDistanceMeters}
          />
        ),
      },
      {
        title: 'Ảnh vào',
        dataIndex: 'checkInPhotoUrl',
        render: (url) => (url ? <AuthImage src={url} alt="Check-in" className="attendance-thumb" /> : '—'),
      },
      {
        title: 'Vị trí ra',
        key: 'checkOutLoc',
        render: (_, r) => (
          <LocationInfo
            lat={r.checkOutLatitude}
            lon={r.checkOutLongitude}
            address={r.checkOutAddress}
            distance={r.checkOutDistanceMeters}
          />
        ),
      },
      {
        title: 'Ảnh ra',
        dataIndex: 'checkOutPhotoUrl',
        render: (url) => (url ? <AuthImage src={url} alt="Check-out" className="attendance-thumb" /> : '—'),
      },
    ] : []),
  ];

  const displayColumns = canViewAll ? columns : columns.filter((c) => c.key !== 'name');

  return (
    <div>
      <PageHeader title="Chấm công" subtitle="Theo dõi giờ làm việc và chấm công" />

      {(locationError || (companyLocation && !companyLocation.configured)) && (
        <Alert
          type="warning"
          showIcon
          message={locationError || 'Chưa cấu hình vị trí công ty. Vui lòng liên hệ quản trị viên.'}
          description={!locationReady ? 'Restart backend (mvn spring-boot:run) để cập nhật API và tọa độ 53A Tăng Nhơn Phú.' : undefined}
          style={{ marginBottom: 16 }}
        />
      )}

      <div className="check-in-card">
        <h3>Chấm công hôm nay</h3>
        <p>{dayjs().format('dddd, D MMMM YYYY')}</p>

        <div className="attendance-today-status">
          <Tag color={hasCheckedOut ? 'blue' : hasCheckedIn ? 'green' : 'default'}>
            {todayStatusLabel(todayRecord)}
          </Tag>
          {todayStatus && hasCheckedIn && (
            <Tag color={todayStatus.color}>{todayStatus.label}</Tag>
          )}
        </div>

        <div className="attendance-today-times">
          <div>
            <span className="label">Giờ check-in</span>
            <strong>{todayRecord?.checkInTime ? formatDateTime(todayRecord.checkInTime, 'HH:mm') : '—'}</strong>
          </div>
          <div>
            <span className="label">Giờ check-out</span>
            <strong>{todayRecord?.checkOutTime ? formatDateTime(todayRecord.checkOutTime, 'HH:mm') : '—'}</strong>
          </div>
        </div>

        {(todayRecord?.checkInPhotoUrl || todayRecord?.checkOutPhotoUrl) && (
          <div className="attendance-today-photos">
            {todayRecord?.checkInPhotoUrl && (
              <div className="attendance-photo-block">
                <span>Ảnh check-in</span>
                <AuthImage src={todayRecord.checkInPhotoUrl} alt="Check-in" className="attendance-today-thumb" />
                {todayRecord.checkInLatitude != null && (
                  <div className="attendance-photo-meta">
                    <MapPin size={12} />
                    {formatDistance(todayRecord.checkInDistanceMeters)}
                  </div>
                )}
              </div>
            )}
            {todayRecord?.checkOutPhotoUrl && (
              <div className="attendance-photo-block">
                <span>Ảnh check-out</span>
                <AuthImage src={todayRecord.checkOutPhotoUrl} alt="Check-out" className="attendance-today-thumb" />
                {todayRecord.checkOutLatitude != null && (
                  <div className="attendance-photo-meta">
                    <MapPin size={12} />
                    {formatDistance(todayRecord.checkOutDistanceMeters)}
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {(todayRecord?.checkInAddress || todayRecord?.checkOutAddress) && (
          <div className="attendance-today-locations">
            {todayRecord?.checkInLatitude != null && (
              <div>
                <strong>Vị trí check-in:</strong> {todayRecord.checkInAddress || formatCoords(todayRecord.checkInLatitude, todayRecord.checkInLongitude)}
              </div>
            )}
            {todayRecord?.checkOutLatitude != null && (
              <div>
                <strong>Vị trí check-out:</strong> {todayRecord.checkOutAddress || formatCoords(todayRecord.checkOutLatitude, todayRecord.checkOutLongitude)}
              </div>
            )}
          </div>
        )}

        <div className="attendance-today-actions">
          <Button
            size="large"
            icon={<LogIn size={18} />}
            disabled={!!checkInDisabledReason()}
            onClick={() => setModalMode('check-in')}
            className="attendance-btn-checkin"
          >
            {hasCheckedIn ? 'Đã chấm vào' : 'Check-in'}
          </Button>
          <Button
            size="large"
            icon={<LogOut size={18} />}
            disabled={!!checkOutDisabledReason()}
            onClick={() => setModalMode('check-out')}
            className="attendance-btn-checkout"
          >
            {hasCheckedOut ? 'Đã chấm ra' : 'Check-out'}
          </Button>
        </div>
        {(checkInDisabledReason() || checkOutDisabledReason()) && (
          <p className="attendance-disabled-hint">
            {checkInDisabledReason() || checkOutDisabledReason()}
          </p>
        )}
      </div>

      <div className="filter-bar">
        <RangePicker onChange={(dates) => { setDateRange(dates); setPage(0); }} />
      </div>

      <div className="data-table-card">
        <Table
          columns={displayColumns}
          dataSource={records}
          rowKey="id"
          loading={loading}
          scroll={{ x: canViewAll ? 1200 : undefined }}
          locale={{ emptyText: 'Chưa có dữ liệu chấm công. Chọn công ty "NexaHR Demo" hoặc chấm công hôm nay.' }}
          pagination={{ current: page + 1, total, pageSize: 10, onChange: (p) => setPage(p - 1) }}
        />
      </div>

      <AttendanceCheckModal
        open={!!modalMode}
        mode={modalMode}
        companyLocation={companyLocation}
        onClose={() => setModalMode(null)}
        onSuccess={handleSuccess}
      />
    </div>
  );
};

export default AttendancePage;
