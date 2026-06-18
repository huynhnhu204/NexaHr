package com.nexahr.dto.response;

import com.nexahr.entity.enums.InterviewMode;
import com.nexahr.entity.enums.InterviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InterviewResponse {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private Long interviewerId;
    private String interviewerName;
    private LocalDateTime scheduledAt;
    private Integer duration;
    private InterviewMode mode;
    private String location;
    private String meetingLink;
    private InterviewStatus status;
    private String evaluation;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
