import { Button, Result } from 'antd';
import { useNavigate } from 'react-router-dom';

const ForbiddenPage = () => {
  const navigate = useNavigate();

  return (
    <div style={{ padding: '48px 24px' }}>
      <Result
        status="403"
        title="Không có quyền truy cập"
        subTitle="Bạn không có quyền xem trang này. Liên hệ quản trị viên nếu cần hỗ trợ."
        extra={(
          <Button type="primary" onClick={() => navigate('/dashboard')}>
            Về trang chủ
          </Button>
        )}
      />
    </div>
  );
};

export default ForbiddenPage;
