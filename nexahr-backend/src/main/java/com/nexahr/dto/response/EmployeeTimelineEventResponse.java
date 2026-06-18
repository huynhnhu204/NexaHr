package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeTimelineEventResponse {
    private String type;
    private String title;
    private String description;
    private LocalDateTime occurredAt;
}
