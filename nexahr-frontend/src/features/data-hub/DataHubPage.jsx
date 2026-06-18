import { useEffect, useState } from 'react';
import { Card, Button, Upload, message, Alert, Descriptions, Space } from 'antd';
import { Download, Upload as UploadIcon, Database } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { API_BASE_URL } from '../../utils/constants';
import { ENDPOINTS } from '../../services/apiEndpoints';

const downloadBlob = async (endpoint, filename) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`${API_BASE_URL}${endpoint}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!res.ok) throw new Error('Tải file thất bại');
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  window.URL.revokeObjectURL(url);
};

const DataHubPage = () => {
  const [importResult, setImportResult] = useState(null);
  const [uploading, setUploading] = useState(false);

  const exportEmployees = async () => {
    try {
      await downloadBlob(ENDPOINTS.DATA_HUB.EXPORT_EMPLOYEES, 'employees.csv');
      message.success('Đã xuất danh sách nhân viên');
    } catch (err) {
      message.error(err.message);
    }
  };

  const downloadTemplate = async () => {
    try {
      await downloadBlob(ENDPOINTS.DATA_HUB.IMPORT_TEMPLATE, 'employee-import-template.csv');
    } catch (err) {
      message.error(err.message);
    }
  };

  const uploadProps = {
    accept: '.csv',
    showUploadList: false,
    customRequest: async ({ file, onSuccess, onError }) => {
      setUploading(true);
      try {
        const formData = new FormData();
        formData.append('file', file);
        const res = await axiosClient.post(ENDPOINTS.DATA_HUB.IMPORT_EMPLOYEES, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
        setImportResult(res.data);
        message.success(`Import xong: ${res.data?.successCount}/${res.data?.totalRows} thành công`);
        onSuccess?.(res);
      } catch (err) {
        message.error(err.message);
        onError?.(err);
      } finally {
        setUploading(false);
      }
    },
  };

  return (
    <div>
      <PageHeader title="Trung tâm dữ liệu" subtitle="Xuất / nhập nhân viên hàng loạt qua CSV" />

      <Alert
        type="info"
        showIcon
        icon={<Database size={16} />}
        message="Import / Export"
        description="Tải template CSV, điền dữ liệu nhân viên và upload. Email trùng sẽ báo lỗi từng dòng."
        style={{ marginBottom: 16 }}
      />

      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <Card title="Xuất dữ liệu">
          <Button icon={<Download size={16} />} onClick={exportEmployees}>
            Xuất danh sách nhân viên (CSV)
          </Button>
        </Card>

        <Card title="Nhập dữ liệu">
          <Space wrap>
            <Button icon={<Download size={16} />} onClick={downloadTemplate}>
              Tải template CSV
            </Button>
            <Upload {...uploadProps}>
              <Button type="primary" icon={<UploadIcon size={16} />} loading={uploading}>
                Upload CSV nhân viên
              </Button>
            </Upload>
          </Space>
        </Card>

        {importResult && (
          <Card title="Kết quả import">
            <Descriptions column={2} size="small" bordered>
              <Descriptions.Item label="Tổng dòng">{importResult.totalRows}</Descriptions.Item>
              <Descriptions.Item label="Thành công">{importResult.successCount}</Descriptions.Item>
              <Descriptions.Item label="Lỗi" span={2}>{importResult.errorCount}</Descriptions.Item>
            </Descriptions>
            {importResult.errors?.length > 0 && (
              <ul style={{ marginTop: 12, color: '#DC2626' }}>
                {importResult.errors.map((e) => <li key={e}>{e}</li>)}
              </ul>
            )}
          </Card>
        )}
      </Space>
    </div>
  );
};

export default DataHubPage;
