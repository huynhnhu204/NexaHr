package com.nexahr.dto.response;

import com.nexahr.entity.enums.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobPostingResponse {
    private Long id;
    private String title;
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionName;
    private String description;
    private String requirement;
    private String salaryRange;
    private JobStatus status;
    private boolean publishedToCareers;
    private int candidateCount;
    private LocalDateTime createdAt;
}
