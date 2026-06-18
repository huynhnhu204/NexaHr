import axiosClient from '../../../services/axiosClient';
import { ENDPOINTS } from '../../../services/apiEndpoints';

const interviewService = {
  getAll: (params) => axiosClient.get(ENDPOINTS.INTERVIEWS.BASE, { params }),
  getById: (id) => axiosClient.get(`${ENDPOINTS.INTERVIEWS.BASE}/${id}`),
  create: (data) => axiosClient.post(ENDPOINTS.INTERVIEWS.BASE, data),
  update: (id, data) => axiosClient.put(`${ENDPOINTS.INTERVIEWS.BASE}/${id}`, data),
  delete: (id) => axiosClient.delete(`${ENDPOINTS.INTERVIEWS.BASE}/${id}`),
  complete: (id, data) => axiosClient.put(ENDPOINTS.INTERVIEWS.COMPLETE(id), data).catch(() =>
    axiosClient.put(`${ENDPOINTS.INTERVIEWS.BASE}/${id}`, { ...data, status: 'COMPLETED' })
  ),
};

export default interviewService;
