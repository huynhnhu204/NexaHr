package com.nexahr.service.impl;

import com.nexahr.dto.request.SamlConfigRequest;
import com.nexahr.dto.request.SamlDemoLoginRequest;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.SamlConfigResponse;
import com.nexahr.dto.response.SamlSsoResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.SamlConfig;
import com.nexahr.entity.User;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.CompanyMembershipRepository;
import com.nexahr.repository.SamlConfigRepository;
import com.nexahr.repository.UserRepository;
import com.nexahr.service.AuthService;
import com.nexahr.service.SamlService;
import com.nexahr.util.PlanFeatureGate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SamlServiceImpl implements SamlService {

    private final SamlConfigRepository samlConfigRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final AuthService authService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public SamlConfigResponse getConfig(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        SamlConfig config = samlConfigRepository.findByCompanyId(companyId).orElse(null);
        return toResponse(company, config);
    }

    @Override
    @Transactional
    public SamlConfigResponse updateConfig(Long companyId, SamlConfigRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        PlanFeatureGate.requireEnterprise(company);

        SamlConfig config = samlConfigRepository.findByCompanyId(companyId)
                .orElse(SamlConfig.builder().company(company).build());

        if (request.getEnabled() != null) config.setEnabled(request.getEnabled());
        if (request.getIdpName() != null) config.setIdpName(request.getIdpName());
        if (request.getEntityId() != null) config.setEntityId(request.getEntityId());
        if (request.getSsoUrl() != null) config.setSsoUrl(request.getSsoUrl());
        if (request.getCertificate() != null) config.setCertificate(request.getCertificate());
        if (request.getAttributeEmail() != null) config.setAttributeEmail(request.getAttributeEmail());

        samlConfigRepository.save(config);
        return toResponse(company, config);
    }

    @Override
    public String getMetadata(String companyCode) {
        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        String entityId = "nexahr-" + company.getCode();
        String acsUrl = frontendUrl + "/api/auth/saml/acs/" + company.getCode();
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" entityID="%s">
                  <md:SPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
                    <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
                      Location="%s" index="0"/>
                  </md:SPSSODescriptor>
                </md:EntityDescriptor>
                """.formatted(entityId, acsUrl);
    }

    @Override
    @Transactional
    public AuthResponse demoLogin(SamlDemoLoginRequest request) {
        Company company = companyRepository.findByCode(request.getCompanyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        SamlConfig config = samlConfigRepository.findByCompanyId(company.getId()).orElse(null);
        if (config == null || !config.isEnabled()) {
            throw new BadRequestException("SAML SSO chưa được bật cho công ty này");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email không tồn tại trong hệ thống"));

        if (!membershipRepository.existsByUserIdAndCompanyId(user.getId(), company.getId())) {
            throw new BadRequestException("Người dùng không thuộc công ty này");
        }

        return authService.issueAuthResponse(user, company.getId());
    }

    @Override
    public SamlSsoResponse getSsoInit(String companyCode) {
        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        SamlConfig config = samlConfigRepository.findByCompanyId(company.getId()).orElse(null);
        boolean enabled = config != null && config.isEnabled();
        String ssoUrl = config != null && config.getSsoUrl() != null && !config.getSsoUrl().isBlank()
                ? config.getSsoUrl()
                : frontendUrl + "/login?saml=" + company.getCode();
        return SamlSsoResponse.builder()
                .enabled(enabled)
                .companyCode(company.getCode())
                .companyName(company.getName())
                .ssoUrl(ssoUrl)
                .demoMode(true)
                .build();
    }

    private SamlConfigResponse toResponse(Company company, SamlConfig config) {
        boolean enterprise = "ENTERPRISE".equals(company.getPlan());
        return SamlConfigResponse.builder()
                .enabled(config != null && config.isEnabled())
                .idpName(config != null ? config.getIdpName() : null)
                .entityId(config != null ? config.getEntityId() : null)
                .ssoUrl(config != null ? config.getSsoUrl() : null)
                .certificate(config != null ? config.getCertificate() : null)
                .attributeEmail(config != null ? config.getAttributeEmail() : "email")
                .metadataUrl("/api/public/saml/" + company.getCode() + "/metadata")
                .acsUrl(frontendUrl + "/login?saml=" + company.getCode())
                .enterpriseRequired(!enterprise)
                .build();
    }
}
