package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogResponse {
    private Long id;
    private String username;
    private String action;
    private String module;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}
