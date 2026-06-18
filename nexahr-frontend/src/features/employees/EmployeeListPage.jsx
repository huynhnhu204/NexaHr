import { useEffect, useState, useMemo } from 'react';
import { Table, Input, Select, Button, Avatar, Space, Modal, message, Popconfirm } from 'antd';
import { Plus, Search, Eye, Edit, Trash2, SlidersHorizontal } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { EMPLOYMENT_STATUS } from '../../utils/constants';
import { useDebounce } from '../../hooks/useDebounce';
import { usePermission } from '../../hooks/usePermission';
import { useUrlFilters } from '../../hooks/useUrlFilters';
import FilterDrawer from '../../components/common/FilterDrawer';
import StatusBadge from '../../components/common/StatusBadge';
import PageHeader from '../../components/common/PageHeader';
import EmployeeForm from './EmployeeForm';

const EmployeeListPage = () => {
  const navigate = useNavigate();
  const { canManageEmployees } = usePermission();
  const { filters, setFilters, resetFilters } = useUrlFilters();
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState(filters.search || '');
  const [departmentId, setDepartmentId] = useState(filters.departmentId || null);
  const [status, setStatus] = useState(filters.status || null);
  const [departments, setDepartments] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [filterOpen, setFilterOpen] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const debouncedSearch = useDebounce(search);

  const fetchEmployees = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 10,
        search: debouncedSearch || undefined,
        departmentId: departmentId ? Number(departmentId) : undefined,
        status: status || undefined,
      };
      const res = await axiosClient.get(ENDPOINTS.EMPLOYEES.BASE, { params });
      const pageData = res?.data ?? res;
      setEmployees(pageData?.content || []);
      setTotal(pageData?.totalElements || 0);
    } catch (err) {
      const msg = err.message || 'Không tải được danh sách nhân viên';
      message.error(msg.includes('Access denied') ? 'Bạn không có quyền xem nhân viên. Đăng nhập bằng tài khoản Admin/HR/Manager.' : msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    axiosClient.get(ENDPOINTS.DEPARTMENTS).then((res) => setDepartments(res.data || []));
  }, []);

  useEffect(() => { fetchEmployees(); }, [page, debouncedSearch, departmentId, status]);

  useEffect(() => {
    setFilters({ search: debouncedSearch, departmentId, status });
  }, [debouncedSearch, departmentId, status]);

  const handleDelete = async (id) => {
    try {
      await axiosClient.delete(`${ENDPOINTS.EMPLOYEES.BASE}/${id}`);
      message.success('Đã vô hiệu hóa nhân viên');
      fetchEmployees();
    } catch (err) {
      message.error(err.message);
    }
  };

  const filterFields = useMemo(() => [
    {
      name: 'departmentId',
      label: 'Phòng ban',
      component: (
        <Select
          allowClear
          placeholder="Chọn phòng ban"
          options={departments.map((d) => ({ value: String(d.id), label: d.name }))}
        />
      ),
    },
    {
      name: 'status',
      label: 'Trạng thái',
      component: (
        <Select
          allowClear
          placeholder="Chọn trạng thái"
          options={Object.entries(EMPLOYMENT_STATUS).map(([k, v]) => ({ value: k, label: v.label }))}
        />
      ),
    },
  ], [departments]);

  const columns = [
    {
      title: 'Nhân viên',
      key: 'employee',
      render: (_, r) => (
        <Space>
          <Avatar style={{ backgroundColor: '#2563eb' }}>{r.fullName?.charAt(0)}</Avatar>
          <div>
            <div style={{ fontWeight: 500 }}>{r.fullName}</div>
            <div style={{ fontSize: 12, color: '#64748b' }}>{r.email}</div>
          </div>
        </Space>
      ),
    },
    { title: 'Mã NV', dataIndex: 'employeeCode', key: 'code' },
    { title: 'Phòng ban', dataIndex: 'departmentName', key: 'department', render: (v) => v || '-' },
    { title: 'Chức vụ', dataIndex: 'positionName', key: 'position', render: (v) => v || '-' },
    {
      title: 'Trạng thái',
      dataIndex: 'employmentStatus',
      key: 'status',
      render: (s) => <StatusBadge status={s} map={EMPLOYMENT_STATUS} />,
    },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, r) => (
        <Space>
          <Button type="text" icon={<Eye size={16} />} onClick={() => navigate(`/employees/${r.id}`)} />
          {canManageEmployees && (
            <>
              <Button type="text" icon={<Edit size={16} />} onClick={() => { setEditingEmployee(r); setModalOpen(true); }} />
              <Popconfirm title="Vô hiệu hóa nhân viên này?" okText="Xóa" cancelText="Hủy" onConfirm={() => handleDelete(r.id)}>
                <Button type="text" danger icon={<Trash2 size={16} />} />
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Nhân viên"
        subtitle="Quản lý đội ngũ nhân sự"
        extra={canManageEmployees && (
          <Button type="primary" icon={<Plus size={16} />} onClick={() => { setEditingEmployee(null); setModalOpen(true); }}
            style={{ background: '#1E3A8A' }}>
            Thêm nhân viên
          </Button>
        )}
      />

      <div className="filter-bar">
        <Input prefix={<Search size={16} />} placeholder="Tìm theo tên hoặc email" value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }} style={{ width: 280 }} allowClear />
        <Button icon={<SlidersHorizontal size={16} />} onClick={() => setFilterOpen(true)}>
          Bộ lọc
        </Button>
        {(departmentId || status) && (
          <Button type="link" onClick={() => { setDepartmentId(null); setStatus(null); resetFilters(); setPage(0); }}>
            Xóa bộ lọc
          </Button>
        )}
      </div>

      <div className="data-table-card table-responsive">
        <Table columns={columns} dataSource={employees} rowKey="id" loading={loading}
          locale={{ emptyText: 'Chưa có nhân viên. Hãy chọn công ty "NexaHR Demo" hoặc đăng nhập lại.' }}
          pagination={{ current: page + 1, total, pageSize: 10, onChange: (p) => setPage(p - 1) }} />
      </div>

      <FilterDrawer
        open={filterOpen}
        onClose={() => setFilterOpen(false)}
        fields={filterFields}
        values={{ departmentId, status }}
        onApply={(vals) => {
          setDepartmentId(vals.departmentId || null);
          setStatus(vals.status || null);
          setPage(0);
        }}
        onReset={() => { setDepartmentId(null); setStatus(null); resetFilters(); setPage(0); }}
      />

      <Modal title={editingEmployee ? 'Sửa nhân viên' : 'Thêm nhân viên'} open={modalOpen}
        onCancel={() => setModalOpen(false)} footer={null} width={640} destroyOnClose>
        <EmployeeForm employee={editingEmployee} departments={departments}
          onSuccess={() => { setModalOpen(false); fetchEmployees(); }} />
      </Modal>
    </div>
  );
};

export default EmployeeListPage;
