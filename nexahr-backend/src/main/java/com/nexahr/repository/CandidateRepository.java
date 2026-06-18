package com.nexahr.repository;

import com.nexahr.entity.Candidate;
import com.nexahr.entity.enums.CandidateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Page<Candidate> findByJobPostingId(Long jobId, Pageable pageable);

    @Query("SELECT c FROM Candidate c WHERE " +
           "(:jobId IS NULL OR c.jobPosting.id = :jobId) " +
           "AND (:status IS NULL OR c.status = :status)")
    Page<Candidate> findWithFilters(@Param("jobId") Long jobId,
                                    @Param("status") CandidateStatus status,
                                    Pageable pageable);

    @Query("SELECT c.status, COUNT(c) FROM Candidate c WHERE c.jobPosting.company.id = :companyId GROUP BY c.status")
    List<Object[]> countByStatusAndCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.jobPosting.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);
}
