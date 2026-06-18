package com.nexahr.dto.response;

import com.nexahr.entity.enums.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrainingEnrollmentResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus status;
    private LocalDateTime completedAt;
    private Integer score;
}
