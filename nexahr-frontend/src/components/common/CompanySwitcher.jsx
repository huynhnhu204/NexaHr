import { useEffect, useState } from 'react';
import { Dropdown, Space, message } from 'antd';
import { Building2, ChevronDown, Check } from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { switchCompany } from '../../features/auth/authSlice';

const CompanySwitcher = () => {
  const dispatch = useDispatch();
  const { user, company } = useSelector((state) => state.auth);
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await axiosClient.get(ENDPOINTS.COMPANIES.MY);
        setCompanies(res.data || []);
      } catch { /* ignore */ }
    };
    if (user) fetch();
  }, [user]);

  if (!companies.length) return null;

  const current = company || companies.find((c) => c.isDefault) || companies[0];

  const handleSwitch = async (companyId) => {
    if (companyId === current?.id) return;
    setLoading(true);
    try {
      await dispatch(switchCompany(companyId)).unwrap();
      window.location.reload();
    } catch (err) {
      message.error(err || 'Không thể chuyển công ty. Hãy đăng xuất và đăng nhập lại.');
    } finally {
      setLoading(false);
    }
  };

  const menu = {
    items: companies.map((c) => ({
      key: String(c.id),
      label: (
        <Space>
          {c.id === current?.id && <Check size={14} color="#22C55E" />}
          <span>{c.name}</span>
          <span style={{ color: '#94A3B8', fontSize: 12 }}>{c.code}</span>
        </Space>
      ),
      onClick: () => handleSwitch(c.id),
    })),
  };

  return (
    <Dropdown menu={menu} trigger={['click']} disabled={loading}>
      <div className="company-switcher">
        <Building2 size={16} />
        <span className="company-switcher-name">{current?.name || 'Công ty'}</span>
        <ChevronDown size={14} />
      </div>
    </Dropdown>
  );
};

export default CompanySwitcher;
