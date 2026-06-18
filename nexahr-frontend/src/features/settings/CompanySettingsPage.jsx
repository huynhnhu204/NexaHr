import { useEffect, useState } from 'react';
import { Card, Form, Input, Button, message, ColorPicker, Select, InputNumber, Divider } from 'antd';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';

const CompanySettingsPage = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    axiosClient.get(ENDPOINTS.COMPANIES.SETTINGS)
      .then((res) => {
        const data = res.data;
        form.setFieldsValue({
          ...data,
          primaryColor: data.primaryColor || '#1E3A8A',
        });
      })
      .finally(() => setLoading(false));
  }, [form]);

  const onFinish = async (values) => {
    setSaving(true);
    try {
      const color = typeof values.primaryColor === 'string'
        ? values.primaryColor
        : values.primaryColor?.toHexString?.() || '#1E3A8A';
      await axiosClient.put(ENDPOINTS.COMPANIES.SETTINGS, { ...values, primaryColor: color });
      message.success('Cập nhật cài đặt công ty thành công');
    } catch (err) {
      message.error(err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Cài đặt công ty"
        subtitle="Thương hiệu, thông tin liên hệ và trang tuyển dụng công khai"
      />

      <Card loading={loading} style={{ maxWidth: 720 }}>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item name="name" label="Tên công ty" rules={[{ required: true, message: 'Vui lòng nhập tên công ty' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="logo" label="URL Logo">
            <Input placeholder="https://..." />
          </Form.Item>
          <Form.Item name="address" label="Địa chỉ">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="phone" label="Số điện thoại">
            <Input />
          </Form.Item>
          <Form.Item name="website" label="Website">
            <Input placeholder="https://..." />
          </Form.Item>
          <Form.Item name="billingEmail" label="Email thanh toán">
            <Input type="email" />
          </Form.Item>
          <Form.Item name="careersTagline" label="Slogan trang tuyển dụng">
            <Input placeholder="Cơ hội nghề nghiệp — Gia nhập đội ngũ của chúng tôi" />
          </Form.Item>
          <Form.Item name="primaryColor" label="Màu thương hiệu">
            <ColorPicker showText format="hex" />
          </Form.Item>
          <Form.Item name="timezone" label="Múi giờ">
            <Select options={[
              { value: 'Asia/Ho_Chi_Minh', label: 'Việt Nam (GMT+7)' },
              { value: 'Asia/Bangkok', label: 'Bangkok (GMT+7)' },
              { value: 'Asia/Singapore', label: 'Singapore (GMT+8)' },
              { value: 'UTC', label: 'UTC' },
            ]} />
          </Form.Item>
          <Form.Item name="locale" label="Ngôn ngữ mặc định">
            <Select options={[
              { value: 'vi', label: 'Tiếng Việt' },
              { value: 'en', label: 'English' },
            ]} />
          </Form.Item>
          <Form.Item name="dataRegion" label="Vùng dữ liệu (Data Residency)">
            <Select options={[
              { value: 'AP_SOUTHEAST', label: 'Đông Nam Á (Singapore)' },
              { value: 'AP_NORTHEAST', label: 'Đông Bắc Á (Tokyo)' },
              { value: 'EU_WEST', label: 'Châu Âu (Frankfurt)' },
              { value: 'US_EAST', label: 'Bắc Mỹ (Virginia)' },
            ]} />
          </Form.Item>

          <Divider>Vị trí chấm công</Divider>
          <Form.Item name="latitude" label="Vĩ độ (latitude)" rules={[{ type: 'number', min: -90, max: 90 }]}>
            <InputNumber style={{ width: '100%' }} placeholder="10.8277714" step={0.0001} />
          </Form.Item>
          <Form.Item name="longitude" label="Kinh độ (longitude)" rules={[{ type: 'number', min: -180, max: 180 }]}>
            <InputNumber style={{ width: '100%' }} placeholder="106.7715260" step={0.0001} />
          </Form.Item>
          <Form.Item name="attendanceRadiusMeters" label="Bán kính chấm công (mét)">
            <InputNumber style={{ width: '100%' }} min={50} max={5000} placeholder="300" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={saving}>Lưu thay đổi</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default CompanySettingsPage;
