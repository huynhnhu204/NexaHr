import { useState, useCallback, useEffect } from 'react';
import axiosClient from '../../../services/axiosClient';
import { ENDPOINTS } from '../../../services/apiEndpoints';

export const useAttendanceReport = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({});

  const fetchReport = useCallback(async () => {
    setLoading(true);
    try {
      const params = { size: 100 };
      if (filters.from) params.startDate = filters.from.format('YYYY-MM-DD');
      if (filters.to) params.endDate = filters.to.format('YYYY-MM-DD');
      if (filters.status) params.status = filters.status;
      const res = await axiosClient.get(ENDPOINTS.ATTENDANCE.BASE, { params });
      const pageData = res?.data ?? res;
      setData(pageData?.content || []);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => { fetchReport(); }, [fetchReport]);

  return { data, loading, filters, setFilters, fetchReport };
};
