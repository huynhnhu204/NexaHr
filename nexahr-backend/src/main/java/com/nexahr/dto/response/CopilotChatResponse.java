package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CopilotChatResponse {
    private String reply;
    private List<String> suggestions;
    private boolean llmPowered;
}
