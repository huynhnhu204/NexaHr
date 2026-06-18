import { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import PageHeader from '../../components/common/PageHeader';
import authService from './authService';

const ChangePasswordPage = () => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const onFinish = async (values) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('Mật khẩu xác nhận không khớp');
      return;
    }
    setLoading(true);
    try {
      await authService.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      message.success('Đổi mật khẩu thành công');
      form.resetFields();
    } catch (err) {
      message.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <PageHeader title="Đổi mật khẩu" subtitle="Cập nhật mật khẩu đăng nhập của bạn" />
      <div className="chart-card" style={{ maxWidth: 480 }}>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item name="currentPassword" label="Mật khẩu hiện tại" rules={[{ required: true, message: 'Vui lòng nhập mật khẩu hiện tại' }]}>
            <Input.Password placeholder="Nhập mật khẩu hiện tại" />
          </Form.Item>
          <Form.Item name="newPassword" label="Mật khẩu mới" rules={[{ required: true, min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' }]}>
            <Input.Password placeholder="Nhập mật khẩu mới" />
          </Form.Item>
          <Form.Item name="confirmPassword" label="Xác nhận mật khẩu mới" rules={[{ required: true, message: 'Vui lòng xác nhận mật khẩu' }]}>
            <Input.Password placeholder="Nhập lại mật khẩu mới" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} style={{ background: '#1E3A8A' }}>
              Cập nhật mật khẩu
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
};

export default ChangePasswordPage;
