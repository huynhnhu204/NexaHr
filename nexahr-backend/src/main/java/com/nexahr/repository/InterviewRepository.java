package com.nexahr.repository;

import com.nexahr.entity.Interview;
import com.nexahr.entity.enums.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("SELECT i FROM Interview i WHERE " +
           "(:candidateId IS NULL OR i.candidate.id = :candidateId) " +
           "AND (:interviewerId IS NULL OR i.interviewer.id = :interviewerId) " +
           "AND (:status IS NULL OR i.status = :status)")
    Page<Interview> findWithFilters(@Param("candidateId") Long candidateId,
                                    @Param("interviewerId") Long interviewerId,
                                    @Param("status") InterviewStatus status,
                                    Pageable pageable);
}
