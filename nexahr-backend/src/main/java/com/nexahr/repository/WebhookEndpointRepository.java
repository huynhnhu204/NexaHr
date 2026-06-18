package com.nexahr.repository;

import com.nexahr.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, Long> {
    List<WebhookEndpoint> findByCompanyIdAndActiveTrue(Long companyId);
    List<WebhookEndpoint> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
}
