import { useEffect, useState } from 'react';
import { Tree, Spin } from 'antd';
import { Building2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import EmptyState from '../../components/common/EmptyState';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';

const OrgChartPage = () => {
  const [loading, setLoading] = useState(true);
  const [treeData, setTreeData] = useState([]);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await axiosClient.get(ENDPOINTS.DEPARTMENTS, { params: { size: 100 } });
        const depts = res.data?.content || [];
        setTreeData([{
          title: (
            <div>
              <div className="org-node-title">Công ty NexaHR</div>
              <div className="org-node-meta">Tổ chức</div>
            </div>
          ),
          key: 'root',
          icon: <Building2 size={16} />,
          children: depts.map((d) => ({
            key: String(d.id),
            title: (
              <div>
                <div className="org-node-title">{d.name}</div>
                <div className="org-node-meta">
                  {d.managerName ? `Trưởng phòng: ${d.managerName}` : 'Chưa có trưởng phòng'}
                  {d.employeeCount != null && ` · ${d.employeeCount} nhân viên`}
                </div>
              </div>
            ),
          })),
        }]);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  return (
    <div>
      <PageHeader title="Sơ đồ tổ chức" subtitle="Cấu trúc phòng ban và quan hệ quản lý" />
      <div className="org-tree-card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: 60 }}><Spin /></div>
        ) : treeData[0]?.children?.length ? (
          <Tree showLine defaultExpandAll treeData={treeData} />
        ) : (
          <EmptyState title="Chưa có phòng ban" description="Thêm phòng ban để hiển thị sơ đồ tổ chức." />
        )}
      </div>
    </div>
  );
};

export default OrgChartPage;
