package com.nexahr.repository;

import com.nexahr.entity.CustomRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomRoleRepository extends JpaRepository<CustomRole, Long> {
    List<CustomRole> findByCompanyIdOrderByNameAsc(Long companyId);
    Optional<CustomRole> findByIdAndCompanyId(Long id, Long companyId);
    boolean existsByCompanyIdAndCode(Long companyId, String code);
}
