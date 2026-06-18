import { useState, useCallback, useEffect } from 'react';
import axios from 'axios';
import axiosClient from '../../../services/axiosClient';
import { ENDPOINTS } from '../../../services/apiEndpoints';
import { API_BASE_URL } from '../../../utils/constants';

export const usePayroll = () => {
  const [payrolls, setPayrolls] = useState([]);
  const [payroll, setPayroll] = useState(null);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({ month: null, status: null });

  const fetchPayrolls = useCallback(async (canViewAll = true) => {
    setLoading(true);
    try {
      const endpoint = canViewAll ? ENDPOINTS.PAYROLLS.BASE : ENDPOINTS.PAYROLLS.MY;
      const params = { page, size: 10 };
      if (filters.month) params.month = filters.month;
      if (filters.status) params.status = filters.status;
      const res = await axiosClient.get(endpoint, { params });
      setPayrolls(res.data?.content || []);
      setTotal(res.data?.totalElements || 0);
    } finally {
      setLoading(false);
    }
  }, [page, filters]);

  const fetchPayrollById = useCallback(async (id) => {
    setLoading(true);
    try {
      const res = await axiosClient.get(`${ENDPOINTS.PAYROLLS.BASE}/${id}`);
      setPayroll(res.data);
      return res.data;
    } finally {
      setLoading(false);
    }
  }, []);

  const generatePayroll = async (month) => {
    await axiosClient.post(ENDPOINTS.PAYROLLS.GENERATE, { month });
  };

  const approvePayroll = async (id) => {
    const res = await axiosClient.put(ENDPOINTS.PAYROLLS.APPROVE(id));
    return res.data;
  };

  const markPaid = async (id) => {
    const res = await axiosClient.put(ENDPOINTS.PAYROLLS.MARK_PAID(id));
    return res.data;
  };

  const exportFile = async (id, type) => {
    const endpoint = type === 'pdf' ? ENDPOINTS.PAYROLLS.EXPORT_PDF(id) : ENDPOINTS.PAYROLLS.EXPORT_EXCEL(id);
    const token = localStorage.getItem('token');
    const res = await axios.get(`${API_BASE_URL}${endpoint}`, {
      responseType: 'blob',
      headers: { Authorization: `Bearer ${token}` },
    });
    const ext = type === 'pdf' ? 'pdf' : 'xlsx';
    const mime = type === 'pdf' ? 'application/pdf' : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
    const url = window.URL.createObjectURL(new Blob([res.data], { type: mime }));
    const link = document.createElement('a');
    link.href = url;
    link.download = `payroll-${id}.${ext}`;
    link.click();
    window.URL.revokeObjectURL(url);
  };

  return {
    payrolls,
    payroll,
    loading,
    total,
    page,
    setPage,
    filters,
    setFilters,
    fetchPayrolls,
    fetchPayrollById,
    generatePayroll,
    approvePayroll,
    markPaid,
    exportFile,
  };
};

export default usePayroll;
