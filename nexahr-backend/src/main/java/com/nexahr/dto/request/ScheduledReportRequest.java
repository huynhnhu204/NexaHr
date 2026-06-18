package com.nexahr.dto.request;

import com.nexahr.entity.enums.ReportFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduledReportRequest {
    @NotBlank
    private String name;
    private String reportType;
    @NotNull
    private ReportFrequency frequency;
    @NotBlank
    private String recipientEmails;
}
