package com.nexahr.dto.request;

import com.nexahr.entity.enums.PerformanceRating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PerformanceReviewRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Review period is required")
    private String reviewPeriod;
    private String goals;
    private LocalDate dueDate;
    private BigDecimal score;
    private PerformanceRating rating;
    private String comment;
}
