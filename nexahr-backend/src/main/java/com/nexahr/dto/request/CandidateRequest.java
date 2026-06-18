package com.nexahr.dto.request;

import com.nexahr.entity.enums.CandidateStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CandidateRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private String phone;
    private String cvFile;
    private CandidateStatus status;
    private String note;
}
