package com.nexahr.repository;

import com.nexahr.entity.LeaveRequest;
import com.nexahr.entity.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    long countByStatus(LeaveStatus status);

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = :status AND l.employee.company.id = :companyId")
    long countByStatusAndCompanyId(@Param("status") LeaveStatus status, @Param("companyId") Long companyId);

    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.company.id = :companyId " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:employeeId IS NULL OR l.employee.id = :employeeId)")
    Page<LeaveRequest> findWithFilters(@Param("companyId") Long companyId,
                                       @Param("status") LeaveStatus status,
                                       @Param("employeeId") Long employeeId,
                                       Pageable pageable);

    @Query("SELECT l FROM LeaveRequest l WHERE l.id = :id AND l.employee.company.id = :companyId")
    java.util.Optional<LeaveRequest> findByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);

    @Query("SELECT l.status, COUNT(l) FROM LeaveRequest l GROUP BY l.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT l.status, COUNT(l) FROM LeaveRequest l WHERE l.employee.company.id = :companyId GROUP BY l.status")
    List<Object[]> countByStatusGroupAndCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT l.leaveType, COUNT(l) FROM LeaveRequest l WHERE l.employee.company.id = :companyId GROUP BY l.leaveType")
    List<Object[]> countByLeaveTypeAndCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.employee.company.id = :companyId AND l.status = 'APPROVED' AND l.approvedAt >= :since")
    long countApprovedSince(@Param("companyId") Long companyId, @Param("since") LocalDateTime since);
}
