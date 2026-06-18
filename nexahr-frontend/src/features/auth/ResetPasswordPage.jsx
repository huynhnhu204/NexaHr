import { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import authService from './authService';

const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const token = searchParams.get('token');

  const onFinish = async (values) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('Mật khẩu xác nhận không khớp');
      return;
    }
    setLoading(true);
    try {
      await authService.resetPassword({ token, newPassword: values.newPassword });
      message.success('Đặt lại mật khẩu thành công');
      navigate('/login');
    } catch (err) {
      message.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <div className="auth-card">
        <div className="auth-logo">
          <h1>Liên kết không hợp lệ</h1>
          <p>Token đặt lại mật khẩu không tồn tại hoặc đã hết hạn.</p>
        </div>
        <Link to="/forgot-password">Yêu cầu liên kết mới</Link>
      </div>
    );
  }

  return (
    <div className="auth-card">
      <div className="auth-logo">
        <h1>Đặt lại mật khẩu</h1>
        <p>Nhập mật khẩu mới cho tài khoản của bạn</p>
      </div>
      <Form layout="vertical" onFinish={onFinish} size="large">
        <Form.Item name="newPassword" label="Mật khẩu mới" rules={[{ required: true, min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' }]}>
          <Input.Password placeholder="Nhập mật khẩu mới" />
        </Form.Item>
        <Form.Item name="confirmPassword" label="Xác nhận mật khẩu" rules={[{ required: true, message: 'Vui lòng xác nhận mật khẩu' }]}>
          <Input.Password placeholder="Nhập lại mật khẩu mới" />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#1E3A8A', height: 44 }}>
            Đặt lại mật khẩu
          </Button>
        </Form.Item>
        <div style={{ textAlign: 'center' }}>
          <Link to="/login">Quay lại đăng nhập</Link>
        </div>
      </Form>
    </div>
  );
};

export default ResetPasswordPage;
