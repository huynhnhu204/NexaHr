package com.nexahr.dto.request;

import com.nexahr.entity.enums.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CourseRequest {
    @NotBlank(message = "Course title is required")
    private String title;

    private String description;
    private String instructor;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxParticipants;
    private CourseStatus status;
}
