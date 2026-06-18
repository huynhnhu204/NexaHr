package com.nexahr.tenant;

import com.nexahr.repository.CompanyMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantCompanyResolver {

    private final CompanyMembershipRepository membershipRepository;

    public Long resolveDefaultCompanyId(Long userId) {
        return membershipRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(m -> m.getCompany().getId())
                .or(() -> membershipRepository.findByUserId(userId).stream()
                        .findFirst()
                        .map(m -> m.getCompany().getId()))
                .orElse(null);
    }
}
