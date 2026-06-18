package com.nexahr.repository;

import com.nexahr.entity.Course;
import com.nexahr.entity.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c WHERE " +
           "(:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR c.status = :status)")
    Page<Course> findWithFilters(@Param("search") String search,
                                 @Param("status") CourseStatus status,
                                 Pageable pageable);
}
