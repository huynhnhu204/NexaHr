import { useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';

export const useUrlFilters = (defaults = {}) => {
  const [searchParams, setSearchParams] = useSearchParams();

  const filters = useMemo(() => {
    const result = { ...defaults };
    searchParams.forEach((value, key) => {
      if (value !== '') result[key] = value;
    });
    return result;
  }, [searchParams, defaults]);

  const setFilters = useCallback((next) => {
    const params = new URLSearchParams();
    Object.entries(next).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params.set(key, String(value));
      }
    });
    setSearchParams(params, { replace: true });
  }, [setSearchParams]);

  const resetFilters = useCallback(() => {
    setSearchParams({}, { replace: true });
  }, [setSearchParams]);

  return { filters, setFilters, resetFilters };
};

export default useUrlFilters;
