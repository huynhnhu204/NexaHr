package com.nexahr.repository;

import com.nexahr.entity.Employee;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.entity.enums.Role;
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
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT e FROM Employee e WHERE e.company.id = :companyId AND " +
           "(:search IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:status IS NULL OR e.employmentStatus = :status)")
    Page<Employee> findWithFilters(@Param("companyId") Long companyId,
                                   @Param("search") String search,
                                   @Param("departmentId") Long departmentId,
                                   @Param("status") EmploymentStatus status,
                                   Pageable pageable);

    long countByEmploymentStatusAndCompanyId(EmploymentStatus status, Long companyId);

    long countByCompanyId(Long companyId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.company.id = :companyId AND e.hireDate >= :startDate")
    long countNewEmployeesSince(@Param("companyId") Long companyId, @Param("startDate") LocalDate startDate);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.company.id = :companyId AND e.hireDate >= :start AND e.hireDate <= :end")
    long countHiredBetween(@Param("companyId") Long companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.company.id = :companyId AND e.employmentStatus = com.nexahr.entity.enums.EmploymentStatus.RESIGNED")
    long countResignedByCompanyId(@Param("companyId") Long companyId);

    List<Employee> findByDepartmentIdAndCompanyId(Long departmentId, Long companyId);

    Optional<Employee> findByIdAndCompanyId(Long id, Long companyId);

    @Query("SELECT e FROM Employee e WHERE e.company.id = :companyId AND e.user.role = :role")
    List<Employee> findByCompanyIdAndUserRole(@Param("companyId") Long companyId, @Param("role") Role role);

    @Query("SELECT e FROM Employee e WHERE e.company.id = :companyId AND e.employmentStatus != com.nexahr.entity.enums.EmploymentStatus.RESIGNED")
    List<Employee> findActiveByCompanyId(@Param("companyId") Long companyId);
}
