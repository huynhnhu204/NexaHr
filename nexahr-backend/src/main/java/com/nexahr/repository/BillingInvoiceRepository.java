package com.nexahr.repository;

import com.nexahr.entity.BillingInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, Long> {
    Page<BillingInvoice> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);
    Optional<BillingInvoice> findByStripeSessionId(String stripeSessionId);
    Optional<BillingInvoice> findByInvoiceNumber(String invoiceNumber);
}
