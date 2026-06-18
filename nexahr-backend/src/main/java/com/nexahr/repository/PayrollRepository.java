package com.nexahr.repository;

import com.nexahr.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Page<Payroll> findByEmployeeId(Long employeeId, Pageable pageable);
    Optional<Payroll> findByEmployeeIdAndSalaryMonth(Long employeeId, String salaryMonth);
    List<Payroll> findBySalaryMonth(String salaryMonth);

    @Query("SELECT p FROM Payroll p WHERE p.employee.company.id = :companyId")
    Page<Payroll> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT p FROM Payroll p WHERE p.salaryMonth = :month AND p.employee.company.id = :companyId")
    List<Payroll> findBySalaryMonthAndCompanyId(@Param("month") String month, @Param("companyId") Long companyId);

    @Query("SELECT p FROM Payroll p WHERE p.id = :id AND p.employee.company.id = :companyId")
    Optional<Payroll> findByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p WHERE p.salaryMonth = :month")
    BigDecimal sumNetSalaryByMonth(@Param("month") String month);

    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p WHERE p.salaryMonth = :month AND p.employee.company.id = :companyId")
    BigDecimal sumNetSalaryByMonthAndCompanyId(@Param("month") String month, @Param("companyId") Long companyId);

    @Query("SELECT p.salaryMonth, COALESCE(SUM(p.netSalary), 0) FROM Payroll p GROUP BY p.salaryMonth ORDER BY p.salaryMonth DESC")
    List<Object[]> sumSalaryByMonth();

    @Query("SELECT p.salaryMonth, COALESCE(SUM(p.netSalary), 0) FROM Payroll p WHERE p.employee.company.id = :companyId GROUP BY p.salaryMonth ORDER BY p.salaryMonth DESC")
    List<Object[]> sumSalaryByMonthAndCompanyId(@Param("companyId") Long companyId);
}
