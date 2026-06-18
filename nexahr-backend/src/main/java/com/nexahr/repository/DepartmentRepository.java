package com.nexahr.repository;

import com.nexahr.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByCompanyId(Long companyId);
    Optional<Department> findByNameAndCompanyId(String name, Long companyId);
    boolean existsByNameAndCompanyId(String name, Long companyId);

    long countByCompanyId(Long companyId);

    @Query("SELECT d.name, COUNT(e) FROM Department d LEFT JOIN d.employees e " +
           "WHERE d.company.id = :companyId GROUP BY d.id, d.name")
    List<Object[]> countEmployeesByDepartment(@Param("companyId") Long companyId);
}
