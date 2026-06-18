package com.nexahr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CopilotChatRequest {
    @NotBlank
    private String message;
}
