package com.nexahr.dto.response;

import com.nexahr.entity.enums.CandidateStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CandidateResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String fullName;
    private String email;
    private String phone;
    private String cvFile;
    private CandidateStatus status;
    private String note;
    private LocalDateTime createdAt;
}
