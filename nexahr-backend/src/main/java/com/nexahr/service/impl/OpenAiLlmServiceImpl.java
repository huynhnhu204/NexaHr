package com.nexahr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexahr.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class OpenAiLlmServiceImpl implements LlmService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.enabled:false}")
    private boolean enabled;

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Override
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public Optional<String> chat(String systemPrompt, String userMessage) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "temperature", 0.4,
                    "max_tokens", 600,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("OpenAI returned non-success: {}", response.getStatusCode());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            return content != null && !content.isBlank() ? Optional.of(content.trim()) : Optional.empty();
        } catch (Exception e) {
            log.warn("OpenAI chat failed, falling back to rules: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
