package com.nexahr.repository;

import com.nexahr.entity.ScheduledReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, Long> {
    List<ScheduledReport> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    List<ScheduledReport> findByActiveTrue();
}
