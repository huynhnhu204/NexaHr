package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private boolean pinned;
    private boolean published;
    private LocalDateTime createdAt;
}
