import { useEffect, useMemo, useState } from 'react';
import { Card, Table, Switch, Button, message, Alert, Tag } from 'antd';
import { Shield } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { ROLE_LABELS } from '../../utils/constants';

const ROLES = ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'];

const PermissionsPage = () => {
  const [matrix, setMatrix] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const fetchMatrix = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.PERMISSIONS.MATRIX);
      setMatrix(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchMatrix(); }, []);

  const isGranted = (role, permissionCode) => {
    const grants = matrix?.matrix?.[role] || [];
    const item = grants.find((g) => g.permission === permissionCode);
    return item ? item.granted : false;
  };

  const toggle = async (role, permissionCode, granted) => {
    setSaving(true);
    try {
      const res = await axiosClient.put(ENDPOINTS.PERMISSIONS.MATRIX, [
        { role, permission: permissionCode, granted },
      ]);
      setMatrix(res.data);
      message.success('Đã cập nhật phân quyền');
    } catch (err) {
      message.error(err.message);
      fetchMatrix();
    } finally {
      setSaving(false);
    }
  };

  const columns = useMemo(() => {
    if (!matrix?.permissions) return [];
    const cols = [
      {
        title: 'Quyền',
        dataIndex: 'label',
        key: 'label',
        fixed: 'left',
        width: 220,
        render: (_, record) => (
          <div>
            <div style={{ fontWeight: 500 }}>{record.label}</div>
            <Tag style={{ marginTop: 4 }}>{record.code}</Tag>
          </div>
        ),
      },
    ];
    ROLES.forEach((role) => {
      cols.push({
        title: ROLE_LABELS[role] || role,
        key: role,
        width: 120,
        align: 'center',
        render: (_, record) => (
          <Switch
            checked={isGranted(role, record.code)}
            loading={saving}
            onChange={(v) => toggle(role, record.code, v)}
            disabled={role === 'ADMIN' && record.code === 'PERMISSIONS_MANAGE'}
          />
        ),
      });
    });
    return cols;
  }, [matrix, saving]);

  return (
    <div>
      <PageHeader
        title="Phân quyền nâng cao"
        subtitle="Ma trận quyền theo vai trò — tùy chỉnh cho từng công ty"
      />

      <Alert
        type="info"
        showIcon
        icon={<Shield size={16} />}
        message="RBAC theo công ty"
        description="Mỗi công ty có ma trận phân quyền riêng. Thay đổi có hiệu lực ngay cho user đang đăng nhập sau khi tải lại trang."
        style={{ marginBottom: 16 }}
      />

      <Card>
        <Table
          rowKey="code"
          loading={loading}
          dataSource={matrix?.permissions || []}
          columns={columns}
          pagination={false}
          scroll={{ x: 900 }}
        />
      </Card>
    </div>
  );
};

export default PermissionsPage;
