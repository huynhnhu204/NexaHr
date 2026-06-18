import { useEffect, useState } from 'react';
import { Form, Input, Select, DatePicker, Button, message } from 'antd';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { EMPLOYMENT_STATUS } from '../../utils/constants';
import dayjs from 'dayjs';

const EmployeeForm = ({ employee, departments, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [positions, setPositions] = useState([]);

  useEffect(() => {
    axiosClient.get(ENDPOINTS.POSITIONS).then((res) => setPositions(res.data || []));
  }, []);

  useEffect(() => {
    if (employee) {
      form.setFieldsValue({
        ...employee,
        hireDate: employee.hireDate ? dayjs(employee.hireDate) : null,
        dateOfBirth: employee.dateOfBirth ? dayjs(employee.dateOfBirth) : null,
      });
    }
  }, [employee, form]);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const payload = {
        ...values,
        hireDate: values.hireDate?.format('YYYY-MM-DD'),
        dateOfBirth: values.dateOfBirth?.format('YYYY-MM-DD'),
      };
      if (employee) {
        await axiosClient.put(`${ENDPOINTS.EMPLOYEES.BASE}/${employee.id}`, payload);
        message.success('Cập nhật nhân viên thành công');
      } else {
        await axiosClient.post(ENDPOINTS.EMPLOYEES.BASE, payload);
        message.success('Thêm nhân viên thành công');
      }
      onSuccess?.();
    } catch (err) {
      message.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Form form={form} layout="vertical" onFinish={onFinish}>
      <Form.Item name="fullName" label="Họ và tên" rules={[{ required: true }]}>
        <Input />
      </Form.Item>
      {!employee && (
        <>
          <Form.Item name="email" label="Email">
            <Input placeholder="Tự động tạo nếu để trống" />
          </Form.Item>
          <Form.Item name="password" label="Mật khẩu">
            <Input.Password placeholder="Mặc định: 123456" />
          </Form.Item>
        </>
      )}
      <Form.Item name="phone" label="Số điện thoại"><Input /></Form.Item>
      <Form.Item name="gender" label="Giới tính">
        <Select options={[{ value: 'Male', label: 'Nam' }, { value: 'Female', label: 'Nữ' }, { value: 'Other', label: 'Khác' }]} />
      </Form.Item>
      <Form.Item name="dateOfBirth" label="Ngày sinh"><DatePicker style={{ width: '100%' }} /></Form.Item>
      <Form.Item name="departmentId" label="Phòng ban">
        <Select options={departments.map((d) => ({ value: d.id, label: d.name }))} allowClear />
      </Form.Item>
      <Form.Item name="positionId" label="Chức vụ">
        <Select options={positions.map((p) => ({ value: p.id, label: p.name }))} allowClear />
      </Form.Item>
      <Form.Item name="hireDate" label="Ngày vào làm"><DatePicker style={{ width: '100%' }} /></Form.Item>
      <Form.Item name="employmentStatus" label="Trạng thái">
        <Select options={Object.entries(EMPLOYMENT_STATUS).map(([k, v]) => ({ value: k, label: v.label }))} />
      </Form.Item>
      <Form.Item name="address" label="Địa chỉ"><Input.TextArea rows={2} /></Form.Item>
      <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#1e3a5f' }}>
        {employee ? 'Cập nhật nhân viên' : 'Tạo nhân viên'}
      </Button>
    </Form>
  );
};

export default EmployeeForm;
