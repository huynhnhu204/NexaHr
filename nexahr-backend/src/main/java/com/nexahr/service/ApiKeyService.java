package com.nexahr.service;

import com.nexahr.dto.request.CreateApiKeyRequest;
import com.nexahr.dto.response.ApiKeyResponse;
import com.nexahr.dto.response.CreateApiKeyResponse;

import java.util.List;

public interface ApiKeyService {
    List<ApiKeyResponse> listKeys(Long companyId);
    CreateApiKeyResponse createKey(Long companyId, CreateApiKeyRequest request);
    void revokeKey(Long companyId, Long keyId);
}
