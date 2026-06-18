import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Modal, Upload, Button, Alert, Spin, Typography, Input, message,
} from 'antd';
import { Camera, MapPin, Clock } from 'lucide-react';
import dayjs from 'dayjs';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import {
  calculateDistanceMeters,
  formatCoords,
  formatDistance,
  getCurrentPosition,
  googleMapsUrl,
} from '../../utils/geoUtils';

const { Text, Link } = Typography;
const { TextArea } = Input;

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_SIZE_MB = 5;

const AttendanceCheckModal = ({
  open,
  mode,
  companyLocation,
  onClose,
  onSuccess,
}) => {
  const isCheckIn = mode === 'check-in';
  const title = isCheckIn ? 'Check-in' : 'Check-out';

  const [fileList, setFileList] = useState([]);
  const [position, setPosition] = useState(null);
  const [locating, setLocating] = useState(false);
  const [locationError, setLocationError] = useState(null);
  const [note, setNote] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const reset = useCallback(() => {
    setFileList([]);
    setPosition(null);
    setLocationError(null);
    setNote('');
    setSubmitting(false);
  }, []);

  useEffect(() => {
    if (!open) {
      reset();
      return;
    }
    fetchLocation();
  }, [open, reset]);

  const fetchLocation = async () => {
    setLocating(true);
    setLocationError(null);
    try {
      const pos = await getCurrentPosition();
      setPosition(pos);
    } catch (e) {
      setPosition(null);
      setLocationError(e.message);
    } finally {
      setLocating(false);
    }
  };

  const distance = useMemo(() => {
    if (!position || !companyLocation?.configured) return null;
    return calculateDistanceMeters(
      position.latitude,
      position.longitude,
      companyLocation.latitude,
      companyLocation.longitude,
    );
  }, [position, companyLocation]);

  const withinRadius = useMemo(() => {
    if (!companyLocation?.configured || distance == null) return false;
    const radius = companyLocation.radiusMeters ?? 300;
    return distance <= radius;
  }, [companyLocation, distance]);

  const hasPhoto = fileList.length > 0 && fileList[0].originFileObj;

  const canSubmit = companyLocation?.configured
    && hasPhoto
    && position
    && withinRadius
    && !submitting;

  const beforeUpload = (file) => {
    if (!ALLOWED_TYPES.includes(file.type)) {
      message.error('File không đúng định dạng. Chỉ chấp nhận JPEG, PNG, WebP');
      return Upload.LIST_IGNORE;
    }
    if (file.size > MAX_SIZE_MB * 1024 * 1024) {
      message.error('Ảnh quá lớn. Kích thước tối đa 5MB');
      return Upload.LIST_IGNORE;
    }
    setFileList([{ uid: file.uid, name: file.name, status: 'done', originFileObj: file, thumbUrl: URL.createObjectURL(file) }]);
    return false;
  };

  const handleSubmit = async () => {
    if (!hasPhoto) {
      message.warning('Vui lòng tải lên ảnh trước khi xác nhận.');
      return;
    }
    if (!position) {
      message.warning('Vui lòng lấy vị trí hiện tại.');
      return;
    }
    if (!withinRadius) {
      message.error('Bạn đang ở ngoài phạm vi chấm công của công ty.');
      return;
    }

    setSubmitting(true);
    try {
      const formData = new FormData();
      formData.append('photo', fileList[0].originFileObj);
      formData.append('latitude', String(position.latitude));
      formData.append('longitude', String(position.longitude));
      formData.append('address', formatCoords(position.latitude, position.longitude));
      if (note.trim()) formData.append('note', note.trim());

      const endpoint = isCheckIn ? ENDPOINTS.ATTENDANCE.CHECK_IN : ENDPOINTS.ATTENDANCE.CHECK_OUT;
      const res = await axiosClient.post(endpoint, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      message.success('Chấm công thành công.');
      onSuccess(res?.data ?? res);
      onClose();
    } catch (err) {
      message.error(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={title}
      open={open}
      onCancel={onClose}
      footer={null}
      width={520}
      destroyOnClose
      className="attendance-check-modal"
    >
      {!companyLocation?.configured && (
        <Alert
          type="warning"
          showIcon
          message="Chưa cấu hình vị trí công ty. Vui lòng liên hệ quản trị viên."
          style={{ marginBottom: 16 }}
        />
      )}

      <div className="attendance-modal-section">
        <Text strong><Clock size={14} style={{ marginRight: 6, verticalAlign: 'middle' }} />Giờ hiện tại</Text>
        <div className="attendance-modal-value">{dayjs().format('HH:mm:ss — dddd, D/M/YYYY')}</div>
      </div>

      <div className="attendance-modal-section">
        <Text strong><Camera size={14} style={{ marginRight: 6, verticalAlign: 'middle' }} />Ảnh minh chứng</Text>
        <Upload
          accept="image/jpeg,image/png,image/webp"
          capture="environment"
          listType="picture-card"
          fileList={fileList}
          maxCount={1}
          beforeUpload={beforeUpload}
          onRemove={() => setFileList([])}
        >
          {fileList.length === 0 && (
            <div>
              <Camera size={20} />
              <div style={{ marginTop: 8, fontSize: 12 }}>Chụp / tải ảnh</div>
            </div>
          )}
        </Upload>
      </div>

      <div className="attendance-modal-section">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
          <Text strong><MapPin size={14} style={{ marginRight: 6, verticalAlign: 'middle' }} />Vị trí hiện tại</Text>
          <Button size="small" onClick={fetchLocation} loading={locating}>Lấy vị trí hiện tại</Button>
        </div>

        {locating && <div style={{ textAlign: 'center', padding: 12 }}><Spin tip="Đang lấy vị trí..." /></div>}
        {locationError && <Alert type="error" message={locationError} style={{ marginBottom: 8 }} />}
        {position && (
          <>
            <div className="attendance-modal-value">{formatCoords(position.latitude, position.longitude)}</div>
            {companyLocation?.configured && distance != null && (
              <>
                <div className="attendance-modal-meta">
                  Khoảng cách tới công ty: <strong>{formatDistance(distance)}</strong>
                  {' · '}
                  {withinRadius ? (
                    <Text type="success">Trong phạm vi chấm công</Text>
                  ) : (
                    <Text type="danger">Ngoài phạm vi chấm công</Text>
                  )}
                </div>
                {!withinRadius && (
                  <Alert
                    type="error"
                    showIcon
                    message="Bạn đang ở ngoài phạm vi chấm công của công ty."
                    style={{ marginTop: 8 }}
                  />
                )}
                <Link href={googleMapsUrl(position.latitude, position.longitude)} target="_blank" rel="noreferrer">
                  Mở trên Google Maps
                </Link>
              </>
            )}
          </>
        )}
      </div>

      <div className="attendance-modal-section">
        <Text strong>Ghi chú (tùy chọn)</Text>
        <TextArea rows={2} value={note} onChange={(e) => setNote(e.target.value)} placeholder="Ghi chú thêm..." style={{ marginTop: 8 }} />
      </div>

      <div className="attendance-modal-actions">
        <div className="attendance-submit-hints">
          <div className={hasPhoto ? 'ok' : ''}>• {hasPhoto ? '✓' : '○'} Ảnh minh chứng</div>
          <div className={position ? 'ok' : ''}>• {position ? '✓' : '○'} Vị trí GPS</div>
          <div className={withinRadius ? 'ok' : ''}>• {withinRadius ? '✓' : '○'} Trong phạm vi {companyLocation?.radiusMeters ?? 300}m</div>
        </div>
        <div className="attendance-modal-buttons">
          <Button onClick={onClose}>Hủy</Button>
          <Button type="primary" loading={submitting} disabled={!canSubmit} onClick={handleSubmit}>
            Xác nhận {title}
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default AttendanceCheckModal;
