package com.nexahr.repository;

import com.nexahr.entity.JobPosting;
import com.nexahr.entity.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByCompany_CodeAndStatusAndPublishedToCareersTrue(String companyCode, JobStatus status);
    List<JobPosting> findByCompanyId(Long companyId);

    Optional<JobPosting> findByIdAndCompany_CodeAndPublishedToCareersTrue(Long id, String companyCode);
    Page<JobPosting> findByStatus(JobStatus status, Pageable pageable);

    long countByStatus(JobStatus status);

    @Query("SELECT COUNT(j) FROM JobPosting j WHERE j.status = :status AND j.department.company.id = :companyId")
    long countByStatusAndCompanyId(@Param("status") JobStatus status, @Param("companyId") Long companyId);

    @Query("SELECT FUNCTION('DATE_FORMAT', j.createdAt, '%Y-%m'), COUNT(j) FROM JobPosting j GROUP BY FUNCTION('DATE_FORMAT', j.createdAt, '%Y-%m') ORDER BY FUNCTION('DATE_FORMAT', j.createdAt, '%Y-%m')")
    List<Object[]> countByMonth();

    @Query("SELECT FUNCTION('DATE_FORMAT', j.createdAt, '%Y-%m'), COUNT(j) FROM JobPosting j WHERE j.department.company.id = :companyId GROUP BY FUNCTION('DATE_FORMAT', j.createdAt, '%Y-%m') ORDER BY FUNCTION('DATE_FORMAT', j.createdAt, '%Y-%m')")
    List<Object[]> countByMonthAndCompanyId(@Param("companyId") Long companyId);
}
