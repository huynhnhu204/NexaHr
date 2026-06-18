import { useAuth } from './useAuth';
import { usePermissions } from './usePermissions';

export const usePermission = () => {
  const { role } = useAuth();
  const { hasPermission } = usePermissions();

  const hasRole = (...roles) => roles.includes(role);
  const isAdmin = role === 'ADMIN';
  const isHR = role === 'HR';
  const isManager = role === 'MANAGER';
  const isEmployee = role === 'EMPLOYEE';
  const canManageEmployees = hasPermission('EMPLOYEE_MANAGE') || hasRole('ADMIN', 'HR');
  const canApproveLeave = hasPermission('LEAVE_APPROVE') || hasRole('ADMIN', 'HR', 'MANAGER');
  const canViewAllPayroll = hasPermission('PAYROLL_VIEW_ALL') || hasRole('ADMIN', 'HR');

  return {
    role,
    hasRole,
    hasPermission,
    isAdmin,
    isHR,
    isManager,
    isEmployee,
    canManageEmployees,
    canApproveLeave,
    canViewAllPayroll,
  };
};
