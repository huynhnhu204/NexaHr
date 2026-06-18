import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { getCurrentUser } from '../features/auth/authSlice';

export const useAuth = () => {
  const dispatch = useDispatch();
  const { user, token, isAuthenticated, loading, error } = useSelector((state) => state.auth);

  useEffect(() => {
    if (token && !user?.email) {
      dispatch(getCurrentUser());
    }
  }, [token, user, dispatch]);

  return { user, token, isAuthenticated, loading, error, role: user?.role };
};
