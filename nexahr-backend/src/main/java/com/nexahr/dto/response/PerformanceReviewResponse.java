package com.nexahr.dto.response;

import com.nexahr.entity.enums.PerformanceRating;
import com.nexahr.entity.enums.PerformanceReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PerformanceReviewResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long reviewerId;
    private String reviewerName;
    private String reviewPeriod;
    private BigDecimal score;
    private PerformanceRating rating;
    private PerformanceReviewStatus status;
    private String goals;
    private String employeeSelfComment;
    private LocalDate dueDate;
    private String comment;
    private LocalDateTime createdAt;
}
