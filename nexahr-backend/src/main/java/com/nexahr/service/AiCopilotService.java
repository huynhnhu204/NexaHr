package com.nexahr.service;

import com.nexahr.dto.request.CopilotChatRequest;
import com.nexahr.dto.response.AiInsightResponse;
import com.nexahr.dto.response.CopilotChatResponse;

import java.util.List;

public interface AiCopilotService {
    List<AiInsightResponse> getInsights(Long companyId);
    CopilotChatResponse chat(Long companyId, CopilotChatRequest request);
}
