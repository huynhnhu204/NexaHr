import { useState } from 'react';
import { Form, Input, Button, message, Alert } from 'antd';
import { Link } from 'react-router-dom';
import authService from './authService';

const ForgotPasswordPage = () => {
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [resetToken, setResetToken] = useState(null);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const res = await authService.forgotPassword(values.email);
      setSent(true);
      if (res?.resetToken) setResetToken(res.resetToken);
      message.success('Đã gửi hướng dẫn đặt lại mật khẩu');
    } catch (err) {
      message.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-card">
      <div className="auth-logo">
        <h1>Quên mật khẩu</h1>
        <p>Nhập email để nhận liên kết đặt lại mật khẩu</p>
      </div>
      {sent ? (
        <div>
          <Alert type="success" message="Kiểm tra email của bạn để đặt lại mật khẩu." showIcon style={{ marginBottom: 16 }} />
          {resetToken && (
            <Alert
              type="info"
              message="Demo: Sử dụng liên kết bên dưới"
              description={(
                <Link to={`/reset-password?token=${resetToken}`}>
                  Đặt lại mật khẩu
                </Link>
              )}
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}
          <Link to="/login">Quay lại đăng nhập</Link>
        </div>
      ) : (
        <Form layout="vertical" onFinish={onFinish} size="large">
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email', message: 'Vui lòng nhập email hợp lệ' }]}>
            <Input placeholder="admin@nexahr.com" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#1E3A8A', height: 44 }}>
              Gửi yêu cầu
            </Button>
          </Form.Item>
          <div style={{ textAlign: 'center' }}>
            <Link to="/login">Quay lại đăng nhập</Link>
          </div>
        </Form>
      )}
    </div>
  );
};

export default ForgotPasswordPage;
