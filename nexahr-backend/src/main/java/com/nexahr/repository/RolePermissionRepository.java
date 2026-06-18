package com.nexahr.repository;

import com.nexahr.entity.RolePermission;
import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByCompanyId(Long companyId);
    List<RolePermission> findByCompanyIdAndRoleAndGrantedTrue(Long companyId, Role role);
    Optional<RolePermission> findByCompanyIdAndRoleAndPermission(Long companyId, Role role, PermissionCode permission);
    void deleteByCompanyId(Long companyId);
}
