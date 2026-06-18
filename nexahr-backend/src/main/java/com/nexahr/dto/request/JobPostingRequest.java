package com.nexahr.dto.request;

import com.nexahr.entity.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobPostingRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private Long departmentId;
    private Long positionId;
    private String description;
    private String requirement;
    private String salaryRange;
    private JobStatus status;
    private Boolean publishedToCareers;
}
