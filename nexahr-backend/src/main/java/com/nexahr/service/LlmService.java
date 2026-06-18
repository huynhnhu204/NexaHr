package com.nexahr.service;

import java.util.Optional;

public interface LlmService {
    boolean isEnabled();
    String getModel();
    Optional<String> chat(String systemPrompt, String userMessage);
}
