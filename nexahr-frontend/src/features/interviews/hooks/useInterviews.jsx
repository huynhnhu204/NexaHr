import { useState, useCallback, useEffect } from 'react';
import interviewService from '../services/interviewService';

export const useInterviews = () => {
  const [interviews, setInterviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({});

  const fetchInterviews = useCallback(async () => {
    setLoading(true);
    try {
      const res = await interviewService.getAll({ page, size: 10, ...filters });
      setInterviews(res.data?.content || []);
      setTotal(res.data?.totalElements || 0);
    } finally {
      setLoading(false);
    }
  }, [page, filters]);

  useEffect(() => { fetchInterviews(); }, [fetchInterviews]);

  const createInterview = async (data) => {
    await interviewService.create(data);
    fetchInterviews();
  };

  const updateInterview = async (id, data) => {
    await interviewService.update(id, data);
    fetchInterviews();
  };

  const deleteInterview = async (id) => {
    await interviewService.delete(id);
    fetchInterviews();
  };

  const completeInterview = async (id, data) => {
    await interviewService.complete(id, data);
    fetchInterviews();
  };

  return {
    interviews,
    loading,
    total,
    page,
    setPage,
    filters,
    setFilters,
    fetchInterviews,
    createInterview,
    updateInterview,
    deleteInterview,
    completeInterview,
  };
};

export default useInterviews;
