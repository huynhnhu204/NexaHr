package com.nexahr.dto.response;

import com.nexahr.entity.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private Long companyId;
    private String employeeName;
    private LocalDate workDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String checkInPhotoUrl;
    private String checkOutPhotoUrl;
    private Double checkInLatitude;
    private Double checkInLongitude;
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private Double checkInDistanceMeters;
    private Double checkOutDistanceMeters;
    private String checkInAddress;
    private String checkOutAddress;
    private BigDecimal totalHours;
    private AttendanceStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
