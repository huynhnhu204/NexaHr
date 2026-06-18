package com.nexahr.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexahr.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenVerifier {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client.id:}")
    private String googleClientId;

    public boolean isEnabled() {
        return googleClientId != null && !googleClientId.isBlank();
    }

    public String getClientId() {
        return googleClientId;
    }

    public GoogleUserInfo verify(String idToken) {
        if (!isEnabled()) {
            throw new BadRequestException("Google SSO chưa được cấu hình");
        }

        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(response);

            if (node.has("error")) {
                throw new BadRequestException("Token Google không hợp lệ");
            }

            String aud = node.path("aud").asText();
            if (!googleClientId.equals(aud)) {
                throw new BadRequestException("Token Google không khớp ứng dụng");
            }

            String email = node.path("email").asText(null);
            if (email == null || email.isBlank()) {
                throw new BadRequestException("Google không cung cấp email");
            }

            return new GoogleUserInfo(
                    node.path("sub").asText(),
                    email,
                    node.path("name").asText(email),
                    node.path("picture").asText(null)
            );
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new BadRequestException("Không thể xác minh token Google");
        }
    }

    public record GoogleUserInfo(String googleId, String email, String name, String picture) {}
}
