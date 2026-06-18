package com.nexahr.config;

import com.nexahr.entity.ApiKey;
import com.nexahr.repository.ApiKeyRepository;
import com.nexahr.security.ApiKeyPrincipal;
import com.nexahr.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String rawKey = request.getHeader("X-API-Key");
            if (rawKey == null || rawKey.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Thiếu X-API-Key header\"}");
                return;
            }

            if (rawKey.length() < 12) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"API key không hợp lệ\"}");
                return;
            }

            String prefix = rawKey.substring(0, Math.min(12, rawKey.length()));
            ApiKey apiKey = apiKeyRepository.findByKeyPrefixAndActiveTrue(prefix).orElse(null);

            if (apiKey == null || !passwordEncoder.matches(rawKey, apiKey.getKeyHash())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"API key không hợp lệ\"}");
                return;
            }

            if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"API key đã hết hạn\"}");
                return;
            }

            apiKey.setLastUsedAt(LocalDateTime.now());
            apiKeyRepository.save(apiKey);

            TenantContext.setCompanyId(apiKey.getCompany().getId());
            ApiKeyPrincipal principal = new ApiKeyPrincipal(
                    apiKey.getId(),
                    apiKey.getCompany().getId(),
                    apiKey.getName(),
                    apiKey.getScopes()
            );

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
