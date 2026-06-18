package com.nexahr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnouncementRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private Boolean pinned;
    private Boolean published;
}
