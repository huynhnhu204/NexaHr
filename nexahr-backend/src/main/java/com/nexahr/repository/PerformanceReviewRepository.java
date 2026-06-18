package com.nexahr.repository;

import com.nexahr.entity.PerformanceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    Page<PerformanceReview> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<PerformanceReview> findByReviewerId(Long reviewerId, Pageable pageable);
}
