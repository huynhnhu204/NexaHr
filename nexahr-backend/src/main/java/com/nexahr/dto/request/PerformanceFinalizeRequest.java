package com.nexahr.dto.request;

import com.nexahr.entity.enums.PerformanceRating;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PerformanceFinalizeRequest {
    private BigDecimal score;
    private PerformanceRating rating;
    private String comment;
}
