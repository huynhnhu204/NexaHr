package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AnalyticsOverviewResponse {
    private long totalEmployees;
    private long newHiresThisMonth;
    private long resignedThisYear;
    private double turnoverRate;
    private long pendingLeaves;
    private long approvedLeavesThisMonth;
    private BigDecimal payrollCostThisMonth;
    private long openPositions;
    private long totalCandidates;
}
