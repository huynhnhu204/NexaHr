package com.nexahr.repository;

import com.nexahr.entity.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {
    List<CompanyMembership> findByUserId(Long userId);
    Optional<CompanyMembership> findByUserIdAndCompanyId(Long userId, Long companyId);
    Optional<CompanyMembership> findByUserIdAndIsDefaultTrue(Long userId);
    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);
}
