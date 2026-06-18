package com.nexahr.dto.response;

import com.nexahr.entity.enums.CourseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private String instructor;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxParticipants;
    private CourseStatus status;
    private Integer enrollmentCount;
    private LocalDateTime createdAt;
}
