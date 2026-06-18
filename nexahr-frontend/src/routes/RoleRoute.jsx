import { Outlet } from 'react-router-dom';
import { usePermission } from '../hooks/usePermission';
import ForbiddenPage from '../components/common/ForbiddenPage';

const RoleRoute = ({ roles }) => {
  const { hasRole } = usePermission();
  if (!hasRole(...roles)) {
    return <ForbiddenPage />;
  }
  return <Outlet />;
};

export default RoleRoute;
