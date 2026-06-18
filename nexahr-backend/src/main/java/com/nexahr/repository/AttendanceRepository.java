package com.nexahr.repository;

import com.nexahr.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    @Query("SELECT a FROM Attendance a WHERE a.employee.company.id = :companyId " +
           "AND (:employeeId IS NULL OR a.employee.id = :employeeId) " +
           "AND (:startDate IS NULL OR a.workDate >= :startDate) " +
           "AND (:endDate IS NULL OR a.workDate <= :endDate)")
    Page<Attendance> findWithFilters(@Param("companyId") Long companyId,
                                     @Param("employeeId") Long employeeId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     Pageable pageable);

    List<Attendance> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate start, LocalDate end);

    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.employee.company.id = :companyId AND a.workDate >= :start GROUP BY a.status")
    List<Object[]> countByStatusAndCompanySince(@Param("companyId") Long companyId, @Param("start") LocalDate start);
}
