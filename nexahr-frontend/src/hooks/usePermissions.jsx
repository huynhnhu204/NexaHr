import { useEffect, useState } from 'react';
import axiosClient from '../services/axiosClient';
import { ENDPOINTS } from '../services/apiEndpoints';

let cachedPermissions = null;
let fetchPromise = null;

export const loadUserPermissions = async () => {
  if (cachedPermissions) return cachedPermissions;
  if (!fetchPromise) {
    fetchPromise = axiosClient.get(ENDPOINTS.PERMISSIONS.ME)
      .then((res) => {
        cachedPermissions = res.data?.permissions || [];
        return cachedPermissions;
      })
      .catch(() => [])
      .finally(() => { fetchPromise = null; });
  }
  return fetchPromise;
};

export const clearPermissionCache = () => {
  cachedPermissions = null;
};

export const usePermissions = () => {
  const [permissions, setPermissions] = useState(cachedPermissions || []);

  useEffect(() => {
    loadUserPermissions().then(setPermissions);
  }, []);

  const hasPermission = (...codes) => codes.some((code) => permissions.includes(code));

  return { permissions, hasPermission, reload: () => loadUserPermissions().then(setPermissions) };
};
