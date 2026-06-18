import { useState, useCallback, useEffect } from 'react';
import axiosClient from '../../../services/axiosClient';
import { ENDPOINTS } from '../../../services/apiEndpoints';

export const useAssets = () => {
  const [assets, setAssets] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({});

  const fetchAssets = useCallback(async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.ASSETS.BASE, { params: { page, size: 10, ...filters } });
      setAssets(res.data?.content || []);
      setTotal(res.data?.totalElements || 0);
    } finally {
      setLoading(false);
    }
  }, [page, filters]);

  useEffect(() => { fetchAssets(); }, [fetchAssets]);

  const createAsset = async (data) => {
    await axiosClient.post(ENDPOINTS.ASSETS.BASE, data);
    fetchAssets();
  };

  const updateAsset = async (id, data) => {
    await axiosClient.put(`${ENDPOINTS.ASSETS.BASE}/${id}`, data);
    fetchAssets();
  };

  const deleteAsset = async (id) => {
    await axiosClient.delete(`${ENDPOINTS.ASSETS.BASE}/${id}`);
    fetchAssets();
  };

  const assignAsset = async (id, employeeId, note) => {
    await axiosClient.put(ENDPOINTS.ASSETS.ASSIGN(id), { employeeId, note });
    fetchAssets();
  };

  const returnAsset = async (id, note) => {
    await axiosClient.put(ENDPOINTS.ASSETS.RETURN(id), { note });
    fetchAssets();
  };

  const fetchHistory = async (id) => {
    const res = await axiosClient.get(ENDPOINTS.ASSETS.HISTORY(id));
    setHistory(res.data || []);
    return res.data || [];
  };

  return {
    assets,
    history,
    loading,
    total,
    page,
    setPage,
    filters,
    setFilters,
    fetchAssets,
    createAsset,
    updateAsset,
    deleteAsset,
    assignAsset,
    returnAsset,
    fetchHistory,
  };
};

export default useAssets;
