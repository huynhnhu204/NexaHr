import { useEffect, useState } from 'react';
import {
  Table, Button, Select, Modal, Form, Input, DatePicker, InputNumber,
  Space, message, Popconfirm, Drawer,
} from 'antd';
import { Plus, Edit, Trash2, UserPlus, RotateCcw, History } from 'lucide-react';
import dayjs from 'dayjs';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { ASSET_TYPE, ASSET_STATUS } from '../../utils/constants';
import { formatDate, formatDateTime } from '../../utils/formatDate';
import { formatCurrency } from '../../utils/formatCurrency';
import useAssets from './hooks/useAssets';

const AssetsPage = () => {
  const {
    assets, history, loading, total, page, setPage, filters, setFilters,
    createAsset, updateAsset, deleteAsset, assignAsset, returnAsset, fetchHistory,
  } = useAssets();
  const [modalOpen, setModalOpen] = useState(false);
  const [assignOpen, setAssignOpen] = useState(false);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [selected, setSelected] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [form] = Form.useForm();
  const [assignForm] = Form.useForm();

  useEffect(() => {
    axiosClient.get(ENDPOINTS.EMPLOYEES.BASE, { params: { size: 100 } })
      .then((res) => setEmployees(res.data?.content || []));
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue({
      ...record,
      purchaseDate: record.purchaseDate ? dayjs(record.purchaseDate) : null,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload = {
      ...values,
      purchaseDate: values.purchaseDate?.format('YYYY-MM-DD'),
    };
    try {
      if (editing) await updateAsset(editing.id, payload);
      else await createAsset(payload);
      message.success(editing ? 'Cập nhật thành công' : 'Thêm tài sản thành công');
      setModalOpen(false);
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleAssign = async () => {
    const values = await assignForm.validateFields();
    try {
      await assignAsset(selected.id, values.employeeId, values.note);
      message.success('Cấp phát tài sản thành công');
      setAssignOpen(false);
    } catch (err) {
      message.error(err.message);
    }
  };

  const handleReturn = async (record) => {
    try {
      await returnAsset(record.id);
      message.success('Thu hồi tài sản thành công');
    } catch (err) {
      message.error(err.message);
    }
  };

  const columns = [
    { title: 'Mã TS', dataIndex: 'assetCode' },
    { title: 'Tên tài sản', dataIndex: 'name' },
    { title: 'Loại', dataIndex: 'assetType', render: (t) => <StatusBadge status={t} map={ASSET_TYPE} /> },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => <StatusBadge status={s} map={ASSET_STATUS} /> },
    { title: 'Người sử dụng', dataIndex: 'assignedToName', render: (v) => v || '-' },
    { title: 'Giá mua', dataIndex: 'purchasePrice', render: (v) => formatCurrency(v) },
    { title: 'Ngày mua', dataIndex: 'purchaseDate', render: (v) => formatDate(v) },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, record) => (
        <Space wrap>
          <Button type="text" icon={<Edit size={16} />} onClick={() => openEdit(record)} />
          {record.status === 'AVAILABLE' && (
            <Button type="text" icon={<UserPlus size={16} />} onClick={() => { setSelected(record); assignForm.resetFields(); setAssignOpen(true); }} />
          )}
          {record.status === 'ASSIGNED' && (
            <Button type="text" icon={<RotateCcw size={16} />} onClick={() => handleReturn(record)} />
          )}
          <Button type="text" icon={<History size={16} />} onClick={() => { setSelected(record); fetchHistory(record.id); setHistoryOpen(true); }} />
          <Popconfirm title="Xóa tài sản?" onConfirm={() => deleteAsset(record.id)} okText="Xóa" cancelText="Hủy">
            <Button type="text" danger icon={<Trash2 size={16} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Quản lý tài sản"
        subtitle="Laptop, màn hình, thiết bị và lịch sử cấp phát"
        extra={<Button type="primary" icon={<Plus size={16} />} onClick={openCreate} style={{ background: '#1E3A8A' }}>Thêm tài sản</Button>}
      />

      <div className="filter-bar">
        <Select placeholder="Loại tài sản" allowClear style={{ width: 160 }}
          onChange={(v) => { setFilters({ ...filters, type: v }); setPage(0); }}
          options={Object.entries(ASSET_TYPE).map(([k, v]) => ({ value: k, label: v.label }))} />
        <Select placeholder="Trạng thái" allowClear style={{ width: 160 }}
          onChange={(v) => { setFilters({ ...filters, status: v }); setPage(0); }}
          options={Object.entries(ASSET_STATUS).map(([k, v]) => ({ value: k, label: v.label }))} />
      </div>

      <div className="data-table-card table-responsive">
        <Table columns={columns} dataSource={assets} rowKey="id" loading={loading}
          pagination={{ current: page + 1, total, pageSize: 10, onChange: (p) => setPage(p - 1) }}
          locale={{ emptyText: <EmptyState title="Chưa có tài sản" /> }} />
      </div>

      <Modal title={editing ? 'Sửa tài sản' : 'Thêm tài sản'} open={modalOpen} onCancel={() => setModalOpen(false)} onOk={handleSubmit} okText="Lưu">
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Tên tài sản" rules={[{ required: true, message: 'Nhập tên tài sản' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="assetCode" label="Mã tài sản">
            <Input />
          </Form.Item>
          <Form.Item name="assetType" label="Loại" rules={[{ required: true, message: 'Chọn loại' }]}>
            <Select options={Object.entries(ASSET_TYPE).map(([k, v]) => ({ value: k, label: v.label }))} />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="purchaseDate" label="Ngày mua">
            <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" />
          </Form.Item>
          <Form.Item name="purchasePrice" label="Giá mua">
            <InputNumber min={0} style={{ width: '100%' }} formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Cấp phát tài sản" open={assignOpen} onCancel={() => setAssignOpen(false)} onOk={handleAssign} okText="Cấp phát">
        <Form form={assignForm} layout="vertical">
          <Form.Item name="employeeId" label="Nhân viên" rules={[{ required: true, message: 'Chọn nhân viên' }]}>
            <Select showSearch optionFilterProp="label"
              options={employees.map((e) => ({ value: e.id, label: e.fullName }))} />
          </Form.Item>
          <Form.Item name="note" label="Ghi chú">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer title={`Lịch sử cấp phát — ${selected?.name || ''}`} open={historyOpen} onClose={() => setHistoryOpen(false)} width={520}>
        <Table
          dataSource={history}
          rowKey="id"
          pagination={false}
          size="small"
          columns={[
            { title: 'Nhân viên', dataIndex: 'employeeName' },
            { title: 'Cấp phát', dataIndex: 'assignedAt', render: (v) => formatDateTime(v) },
            { title: 'Thu hồi', dataIndex: 'returnedAt', render: (v) => (v ? formatDateTime(v) : '-') },
            { title: 'Ghi chú', dataIndex: 'note' },
          ]}
          locale={{ emptyText: 'Chưa có lịch sử' }}
        />
      </Drawer>
    </div>
  );
};

export default AssetsPage;
