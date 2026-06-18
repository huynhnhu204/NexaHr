import { useEffect } from 'react';
import { Drawer, Form, Button, Space } from 'antd';
import { Filter } from 'lucide-react';

const FilterDrawer = ({
  open,
  onClose,
  fields = [],
  values = {},
  onApply,
  onReset,
  title = 'Bộ lọc nâng cao',
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open) form.setFieldsValue(values);
  }, [open, values, form]);

  const handleApply = () => {
    onApply(form.getFieldsValue());
    onClose();
  };

  const handleReset = () => {
    form.resetFields();
    onReset?.();
    onClose();
  };

  return (
    <Drawer
      title={title}
      open={open}
      onClose={onClose}
      width={360}
      extra={<Filter size={18} color="#64748B" />}
      footer={(
        <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
          <Button onClick={handleReset}>Đặt lại</Button>
          <Button type="primary" onClick={handleApply} style={{ background: '#1E3A8A' }}>
            Áp dụng
          </Button>
        </Space>
      )}
    >
      <Form form={form} layout="vertical">
        {fields.map((field) => (
          <Form.Item key={field.name} name={field.name} label={field.label}>
            {field.component}
          </Form.Item>
        ))}
      </Form>
    </Drawer>
  );
};

export default FilterDrawer;
