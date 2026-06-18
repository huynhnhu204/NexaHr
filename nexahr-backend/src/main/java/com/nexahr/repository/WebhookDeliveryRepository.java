package com.nexahr.repository;

import com.nexahr.entity.WebhookDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    Page<WebhookDelivery> findByWebhookEndpointCompanyIdOrderByAttemptedAtDesc(Long companyId, Pageable pageable);
}
