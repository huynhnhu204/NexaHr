package com.nexahr.repository;

import com.nexahr.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    List<ApiKey> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    Optional<ApiKey> findByKeyPrefixAndActiveTrue(String keyPrefix);
}
