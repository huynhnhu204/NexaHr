package com.nexahr.service.impl;

import com.nexahr.dto.request.CreateApiKeyRequest;
import com.nexahr.dto.response.ApiKeyResponse;
import com.nexahr.dto.response.CreateApiKeyResponse;
import com.nexahr.entity.ApiKey;
import com.nexahr.entity.Company;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.ApiKeyRepository;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.service.ApiKeyService;
import com.nexahr.util.PlanFeatureGate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ApiKeyResponse> listKeys(Long companyId) {
        return apiKeyRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CreateApiKeyResponse createKey(Long companyId, CreateApiKeyRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        PlanFeatureGate.requireProOrEnterprise(company);

        String rawKey = generateRawKey();
        String prefix = rawKey.substring(0, 12);

        ApiKey apiKey = ApiKey.builder()
                .company(company)
                .name(request.getName())
                .keyPrefix(prefix)
                .keyHash(passwordEncoder.encode(rawKey))
                .scopes(request.getScopes() != null ? request.getScopes() : "employees:read,departments:read")
                .expiresAt(LocalDateTime.now().plusYears(1))
                .build();
        apiKeyRepository.save(apiKey);

        return CreateApiKeyResponse.builder()
                .apiKey(toResponse(apiKey))
                .rawKey(rawKey)
                .build();
    }

    @Override
    @Transactional
    public void revokeKey(Long companyId, Long keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy API key"));
        if (!apiKey.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy API key");
        }
        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
    }

    private String generateRawKey() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        String random = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "nexa_live_" + random;
    }

    private ApiKeyResponse toResponse(ApiKey key) {
        return ApiKeyResponse.builder()
                .id(key.getId())
                .name(key.getName())
                .keyPrefix(key.getKeyPrefix() + "...")
                .scopes(key.getScopes())
                .active(key.isActive())
                .lastUsedAt(key.getLastUsedAt())
                .expiresAt(key.getExpiresAt())
                .createdAt(key.getCreatedAt())
                .build();
    }
}
