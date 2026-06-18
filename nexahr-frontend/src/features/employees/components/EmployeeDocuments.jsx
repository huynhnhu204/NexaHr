import { useState } from 'react';
import { Table, Upload, Select, Button, Space, message, Popconfirm } from 'antd';
import { Upload as UploadIcon, Download, Trash2 } from 'lucide-react';
import axios from 'axios';
import { API_BASE_URL } from '../../../utils/constants';
import { ENDPOINTS } from '../../../services/apiEndpoints';
import { DOCUMENT_TYPE } from '../../../utils/constants';
import StatusBadge from '../../../components/common/StatusBadge';
import { formatDateTime } from '../../../utils/formatDate';
import { usePermission } from '../../../hooks/usePermission';
import EmptyState from '../../../components/common/EmptyState';

const EmployeeDocuments = ({ employeeId, documents, onRefresh, onDocumentsChange }) => {
  const { canManageEmployees } = usePermission();
  const [uploading, setUploading] = useState(false);
  const [docType, setDocType] = useState('OTHER');

  const handleUpload = async ({ file }) => {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('documentType', docType);
      const token = localStorage.getItem('token');
      const res = await axios.post(
        `${API_BASE_URL}${ENDPOINTS.EMPLOYEES.documents(employeeId)}`,
        formData,
        { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'multipart/form-data' } }
      );
      const newDoc = res.data?.data ?? res.data;
      onDocumentsChange?.([newDoc, ...documents]);
      message.success('Tải tài liệu thành công');
      onRefresh?.();
    } catch (err) {
      message.error(err.response?.data?.message || err.message || 'Tải tài liệu thất bại');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (docId) => {
    try {
      await axios.delete(
        `${API_BASE_URL}${ENDPOINTS.EMPLOYEES.documentDelete(employeeId, docId)}`,
        { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } }
      );
      onDocumentsChange?.(documents.filter((d) => d.id !== docId));
      message.success('Đã xóa tài liệu');
      onRefresh?.();
    } catch (err) {
      message.error(err.response?.data?.message || err.message);
    }
  };

  const handleDownload = (record) => {
    const url = record.filePath?.startsWith('http')
      ? record.filePath
      : `${API_BASE_URL}${record.filePath}`;
    window.open(url, '_blank');
  };

  const columns = [
    { title: 'Tên tệp', dataIndex: 'originalName', key: 'name' },
    { title: 'Loại', dataIndex: 'documentType', key: 'type', render: (t) => <StatusBadge status={t} map={DOCUMENT_TYPE} /> },
    { title: 'Kích thước', dataIndex: 'fileSize', key: 'size', render: (s) => s ? `${(s / 1024).toFixed(1)} KB` : '-' },
    { title: 'Người tải', dataIndex: 'uploadedByName', key: 'uploader' },
    { title: 'Ngày tải', dataIndex: 'createdAt', key: 'date', render: (v) => formatDateTime(v) },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button type="text" icon={<Download size={16} />} onClick={() => handleDownload(record)} />
          {canManageEmployees && (
            <Popconfirm title="Xóa tài liệu này?" onConfirm={() => handleDelete(record.id)} okText="Xóa" cancelText="Hủy">
              <Button type="text" danger icon={<Trash2 size={16} />} />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      {canManageEmployees && (
        <div className="filter-bar" style={{ marginBottom: 16 }}>
          <Select
            value={docType}
            onChange={setDocType}
            style={{ width: 200 }}
            options={Object.entries(DOCUMENT_TYPE).map(([k, v]) => ({ value: k, label: v.label }))}
          />
          <Upload showUploadList={false} customRequest={handleUpload} accept=".pdf,.doc,.docx,.jpg,.jpeg,.png">
            <Button type="primary" icon={<UploadIcon size={16} />} loading={uploading} style={{ background: '#1E3A8A' }}>
              Tải lên tài liệu
            </Button>
          </Upload>
        </div>
      )}
      <Table
        columns={columns}
        dataSource={documents}
        rowKey="id"
        pagination={false}
        locale={{ emptyText: <EmptyState title="Chưa có tài liệu" description="Tải lên hợp đồng, CV hoặc chứng chỉ." /> }}
      />
    </div>
  );
};

export default EmployeeDocuments;
