import { useState, useCallback, useEffect } from 'react';
import axiosClient from '../../../services/axiosClient';
import { ENDPOINTS } from '../../../services/apiEndpoints';

export const useEmployeeProfile = (employeeId) => {
  const [employee, setEmployee] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [timeline, setTimeline] = useState([]);
  const [attendance, setAttendance] = useState([]);
  const [leaves, setLeaves] = useState([]);
  const [payrolls, setPayrolls] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchProfile = useCallback(async () => {
    if (!employeeId) return;
    setLoading(true);
    try {
      const [empRes, docsRes, timelineRes, attRes, leaveRes, payRes] = await Promise.all([
        axiosClient.get(`${ENDPOINTS.EMPLOYEES.BASE}/${employeeId}`),
        axiosClient.get(ENDPOINTS.EMPLOYEES.documents(employeeId)).catch(() => ({ data: [] })),
        axiosClient.get(ENDPOINTS.EMPLOYEES.timeline(employeeId)).catch(() => ({ data: [] })),
        axiosClient.get(ENDPOINTS.ATTENDANCE.BASE, { params: { employeeId, size: 5 } }),
        axiosClient.get(ENDPOINTS.LEAVES.BASE, { params: { employeeId, size: 5 } }),
        axiosClient.get(ENDPOINTS.PAYROLLS.BASE, { params: { size: 5 } }),
      ]);
      setEmployee(empRes.data);
      setDocuments(docsRes.data || []);
      setTimeline(timelineRes.data || []);
      setAttendance(attRes.data?.content || []);
      setLeaves(leaveRes.data?.content || []);
      setPayrolls((payRes.data?.content || []).filter((p) => p.employeeId === Number(employeeId)));
    } finally {
      setLoading(false);
    }
  }, [employeeId]);

  useEffect(() => { fetchProfile(); }, [fetchProfile]);

  return {
    employee,
    documents,
    timeline,
    attendance,
    leaves,
    payrolls,
    loading,
    fetchProfile,
    setDocuments,
  };
};

export default useEmployeeProfile;
