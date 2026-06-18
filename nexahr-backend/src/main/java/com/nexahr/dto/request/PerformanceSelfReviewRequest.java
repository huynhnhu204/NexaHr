package com.nexahr.dto.request;

import com.nexahr.entity.enums.PerformanceRating;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PerformanceSelfReviewRequest {
    private String employeeSelfComment;
}
