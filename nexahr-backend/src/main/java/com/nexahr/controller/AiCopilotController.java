package com.nexahr.controller;

import com.nexahr.dto.request.CopilotChatRequest;
import com.nexahr.dto.response.AiConfigResponse;
import com.nexahr.dto.response.AiInsightResponse;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.CopilotChatResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.AiCopilotService;
import com.nexahr.service.LlmService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
public class AiCopilotController {

    private final AiCopilotService aiCopilotService;
    private final LlmService llmService;

    @GetMapping("/config")
    public ApiResponse<AiConfigResponse> getConfig() {
        return ApiResponse.success(AiConfigResponse.builder()
                .llmEnabled(llmService.isEnabled())
                .model(llmService.getModel())
                .provider("OpenAI")
                .build());
    }

    @GetMapping("/insights")
    public ApiResponse<List<AiInsightResponse>> getInsights() {
        return ApiResponse.success(aiCopilotService.getInsights(requireCompanyId()));
    }

    @PostMapping("/chat")
    public ApiResponse<CopilotChatResponse> chat(@Valid @RequestBody CopilotChatRequest request) {
        return ApiResponse.success(aiCopilotService.chat(requireCompanyId(), request));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
