package com.nexahr.repository;

import com.nexahr.entity.CustomRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomRolePermissionRepository extends JpaRepository<CustomRolePermission, Long> {
    List<CustomRolePermission> findByCustomRoleIdAndGrantedTrue(Long customRoleId);
}
