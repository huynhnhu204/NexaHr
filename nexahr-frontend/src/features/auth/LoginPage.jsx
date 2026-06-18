import { useEffect } from 'react';
import { Form, Input, Button, Checkbox, message } from 'antd';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { login, clearError } from './authSlice';
import GoogleLoginButton from './GoogleLoginButton';
import SamlLoginButton from './SamlLoginButton';

const LoginPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, isAuthenticated } = useSelector((state) => state.auth);
  const [searchParams] = useSearchParams();
  const sessionExpired = searchParams.get('session') === 'expired';

  const [form] = Form.useForm();

  useEffect(() => {
    if (isAuthenticated) navigate('/dashboard');
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    if (sessionExpired) message.warning('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
  }, [sessionExpired]);

  useEffect(() => {
    if (error) {
      message.error(error);
      dispatch(clearError());
    }
  }, [error, dispatch]);

  const onFinish = async (values) => {
    try {
      await dispatch(login({ email: values.email, password: values.password })).unwrap();
      message.success('Đăng nhập thành công');
      navigate('/dashboard');
    } catch {
      // lỗi hiển thị qua authSlice
    }
  };

  return (
    <div className="auth-card">
      <div className="auth-logo">
        <h1>NexaHR</h1>
        <p>Work Smarter. Manage Better.</p>
      </div>
      <Form form={form} layout="vertical" onFinish={onFinish} size="large">
        <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email', message: 'Vui lòng nhập email' }]}>
          <Input placeholder="admin@nexahr.com" />
        </Form.Item>
        <Form.Item name="password" label="Mật khẩu" rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}>
          <Input.Password placeholder="Nhập mật khẩu" />
        </Form.Item>
        <Form.Item>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Form.Item name="remember" valuePropName="checked" noStyle>
              <Checkbox>Ghi nhớ đăng nhập</Checkbox>
            </Form.Item>
            <Link to="/forgot-password" style={{ color: '#2563eb' }}>Quên mật khẩu?</Link>
          </div>
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block
            style={{ background: '#1e3a5f', height: 44, fontWeight: 600 }}>
            Đăng nhập
          </Button>
        </Form.Item>
      </Form>
      <GoogleLoginButton />
      <SamlLoginButton />
      <div style={{ textAlign: 'center', marginTop: 16, color: '#64748b', fontSize: 13 }}>
        Demo: admin@nexahr.com / 123456
      </div>
    </div>
  );
};

export default LoginPage;
