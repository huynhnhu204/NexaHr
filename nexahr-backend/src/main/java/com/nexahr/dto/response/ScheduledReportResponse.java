package com.nexahr.dto.response;

import com.nexahr.entity.enums.ReportFrequency;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScheduledReportResponse {
    private Long id;
    private String name;
    private String reportType;
    private ReportFrequency frequency;
    private String recipientEmails;
    private boolean active;
    private LocalDateTime lastRunAt;
    private LocalDateTime createdAt;
}
