import { useState, useCallback, useEffect } from 'react';
import axiosClient from '../../../services/axiosClient';
import { ENDPOINTS } from '../../../services/apiEndpoints';

export const useTraining = () => {
  const [courses, setCourses] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [courseTotal, setCourseTotal] = useState(0);
  const [enrollmentTotal, setEnrollmentTotal] = useState(0);
  const [coursePage, setCoursePage] = useState(0);
  const [enrollmentPage, setEnrollmentPage] = useState(0);

  const fetchCourses = useCallback(async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.COURSES.BASE, { params: { page: coursePage, size: 10 } });
      setCourses(res.data?.content || []);
      setCourseTotal(res.data?.totalElements || 0);
    } finally {
      setLoading(false);
    }
  }, [coursePage]);

  const fetchEnrollments = useCallback(async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(ENDPOINTS.ENROLLMENTS.BASE, { params: { page: enrollmentPage, size: 10 } });
      setEnrollments(res.data?.content || []);
      setEnrollmentTotal(res.data?.totalElements || 0);
    } finally {
      setLoading(false);
    }
  }, [enrollmentPage]);

  useEffect(() => { fetchCourses(); }, [fetchCourses]);
  useEffect(() => { fetchEnrollments(); }, [fetchEnrollments]);

  const createCourse = async (data) => {
    await axiosClient.post(ENDPOINTS.COURSES.BASE, data);
    fetchCourses();
  };

  const enrollEmployee = async (courseId, employeeId) => {
    await axiosClient.post(ENDPOINTS.COURSES.ENROLL(courseId), { employeeId });
    fetchEnrollments();
    fetchCourses();
  };

  const updateEnrollmentStatus = async (id, status, score) => {
    await axiosClient.put(ENDPOINTS.ENROLLMENTS.STATUS(id), { status, score });
    fetchEnrollments();
  };

  return {
    courses,
    enrollments,
    loading,
    courseTotal,
    enrollmentTotal,
    coursePage,
    setCoursePage,
    enrollmentPage,
    setEnrollmentPage,
    fetchCourses,
    fetchEnrollments,
    createCourse,
    enrollEmployee,
    updateEnrollmentStatus,
  };
};

export default useTraining;
