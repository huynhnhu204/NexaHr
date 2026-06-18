package com.nexahr.dto.response;

import com.nexahr.entity.enums.LeaveStatus;
import com.nexahr.entity.enums.LeaveType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveStatus status;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectReason;
    private LocalDateTime createdAt;
}
