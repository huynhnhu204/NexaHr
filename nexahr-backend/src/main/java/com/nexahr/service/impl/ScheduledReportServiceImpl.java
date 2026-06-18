package com.nexahr.service.impl;

import com.nexahr.dto.request.ScheduledReportRequest;
import com.nexahr.dto.response.ScheduledReportResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.ScheduledReport;
import com.nexahr.entity.enums.ReportFrequency;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.ScheduledReportRepository;
import com.nexahr.service.AnalyticsService;
import com.nexahr.service.EmailService;
import com.nexahr.service.ScheduledReportService;
import com.nexahr.util.PlanFeatureGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportServiceImpl implements ScheduledReportService {

    private final ScheduledReportRepository scheduledReportRepository;
    private final CompanyRepository companyRepository;
    private final AnalyticsService analyticsService;
    private final EmailService emailService;

    @Override
    public List<ScheduledReportResponse> list(Long companyId) {
        return scheduledReportRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ScheduledReportResponse create(Long companyId, ScheduledReportRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        PlanFeatureGate.requireProOrEnterprise(company);

        ScheduledReport report = ScheduledReport.builder()
                .company(company)
                .name(request.getName())
                .reportType(request.getReportType() != null ? request.getReportType() : "WORKFORCE")
                .frequency(request.getFrequency())
                .recipientEmails(request.getRecipientEmails())
                .build();
        return toResponse(scheduledReportRepository.save(report));
    }

    @Override
    @Transactional
    public void delete(Long companyId, Long id) {
        ScheduledReport report = scheduledReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo cáo"));
        if (!report.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy báo cáo");
        }
        report.setActive(false);
        scheduledReportRepository.save(report);
    }

    @Override
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void runDueReports() {
        for (ScheduledReport report : scheduledReportRepository.findByActiveTrue()) {
            if (isDue(report)) {
                try {
                    deliverReport(report);
                    report.setLastRunAt(LocalDateTime.now());
                    scheduledReportRepository.save(report);
                } catch (Exception e) {
                    log.warn("Scheduled report {} failed: {}", report.getId(), e.getMessage());
                }
            }
        }
    }

    private boolean isDue(ScheduledReport report) {
        if (report.getLastRunAt() == null) return true;
        long hours = ChronoUnit.HOURS.between(report.getLastRunAt(), LocalDateTime.now());
        return switch (report.getFrequency()) {
            case DAILY -> hours >= 24;
            case WEEKLY -> hours >= 168;
            case MONTHLY -> hours >= 720;
        };
    }

    private void deliverReport(ScheduledReport report) {
        var analytics = analyticsService.getAnalytics(report.getCompany().getId());
        var o = analytics.getOverview();
        String body = String.format(
                "Báo cáo %s - %s%n%nNhân viên: %d%nTurnover: %.1f%%%nNghỉ phép chờ: %d%nChi phí lương: %s",
                report.getName(),
                report.getCompany().getName(),
                o.getTotalEmployees(),
                o.getTurnoverRate(),
                o.getPendingLeaves(),
                o.getPayrollCostThisMonth() != null ? o.getPayrollCostThisMonth().toPlainString() : "0"
        );
        for (String email : report.getRecipientEmails().split(",")) {
            emailService.sendNotificationEmail(email.trim(), "NexaHR - " + report.getName(), body);
        }
    }

    private ScheduledReportResponse toResponse(ScheduledReport r) {
        return ScheduledReportResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .reportType(r.getReportType())
                .frequency(r.getFrequency())
                .recipientEmails(r.getRecipientEmails())
                .active(r.isActive())
                .lastRunAt(r.getLastRunAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
