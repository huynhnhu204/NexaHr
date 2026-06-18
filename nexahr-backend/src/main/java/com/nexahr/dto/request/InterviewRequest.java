package com.nexahr.dto.request;

import com.nexahr.entity.enums.InterviewMode;
import com.nexahr.entity.enums.InterviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewRequest {
    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    @NotNull(message = "Interviewer ID is required")
    private Long interviewerId;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    private Integer duration;
    private InterviewMode mode;
    private String location;
    private String meetingLink;
    private InterviewStatus status;
    private String evaluation;
    private String notes;
}
