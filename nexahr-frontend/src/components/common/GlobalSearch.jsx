import { useState, useEffect, useCallback } from 'react';
import { Modal, Input, List, Avatar, Empty, Spin, Tag } from 'antd';
import { Search, Users, Building2, Briefcase } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../../services/axiosClient';
import { useDebounce } from '../../hooks/useDebounce';

const TYPE_CONFIG = {
  employee: { icon: Users, color: '#2563EB', label: 'Nhân viên' },
  department: { icon: Building2, color: '#22C55E', label: 'Phòng ban' },
  position: { icon: Briefcase, color: '#F59E0B', label: 'Chức vụ' },
};

const GlobalSearch = ({ open, onClose }) => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const debouncedQuery = useDebounce(query, 300);
  const navigate = useNavigate();

  const search = useCallback(async (q) => {
    if (!q || q.length < 2) { setResults([]); return; }
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.SEARCH, { params: { q } });
      setResults(res.data || []);
    } catch {
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { search(debouncedQuery); }, [debouncedQuery, search]);

  const handleSelect = (item) => {
    onClose();
    setQuery('');
    if (item.type === 'employee') navigate(`/employees/${item.id}`);
    else if (item.type === 'department') navigate('/departments');
    else if (item.type === 'position') navigate('/positions');
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      width={560}
      className="global-search-modal"
      title={null}
      closable={false}
    >
      <Input
        size="large"
        prefix={<Search size={18} color="#94A3B8" />}
        placeholder="Tìm nhân viên, phòng ban, chức vụ..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        autoFocus
        allowClear
      />
      <div className="global-search-results">
        {loading ? (
          <div style={{ textAlign: 'center', padding: 32 }}><Spin /></div>
        ) : results.length === 0 ? (
          <Empty description={query.length < 2 ? 'Nhập ít nhất 2 ký tự' : 'Không tìm thấy kết quả'} image={Empty.PRESENTED_IMAGE_SIMPLE} />
        ) : (
          <List
            dataSource={results}
            renderItem={(item) => {
              const cfg = TYPE_CONFIG[item.type] || TYPE_CONFIG.employee;
              const Icon = cfg.icon;
              return (
                <List.Item className="search-result-item" onClick={() => handleSelect(item)}>
                  <List.Item.Meta
                    avatar={<Avatar style={{ background: cfg.color }} icon={<Icon size={16} />} />}
                    title={item.title}
                    description={item.subtitle}
                  />
                  <Tag>{cfg.label}</Tag>
                </List.Item>
              );
            }}
          />
        )}
      </div>
    </Modal>
  );
};

export default GlobalSearch;
