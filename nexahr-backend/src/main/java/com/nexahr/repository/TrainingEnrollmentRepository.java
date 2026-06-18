package com.nexahr.repository;

import com.nexahr.entity.TrainingEnrollment;
import com.nexahr.entity.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollment, Long> {
    Optional<TrainingEnrollment> findByCourseIdAndEmployeeId(Long courseId, Long employeeId);

    @Query("SELECT e FROM TrainingEnrollment e WHERE " +
           "(:courseId IS NULL OR e.course.id = :courseId) " +
           "AND (:employeeId IS NULL OR e.employee.id = :employeeId) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<TrainingEnrollment> findWithFilters(@Param("courseId") Long courseId,
                                             @Param("employeeId") Long employeeId,
                                             @Param("status") EnrollmentStatus status,
                                             Pageable pageable);
}
