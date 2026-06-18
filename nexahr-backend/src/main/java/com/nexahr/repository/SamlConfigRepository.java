package com.nexahr.repository;

import com.nexahr.entity.SamlConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SamlConfigRepository extends JpaRepository<SamlConfig, Long> {
    Optional<SamlConfig> findByCompanyId(Long companyId);
    Optional<SamlConfig> findByCompany_Code(String companyCode);
}
