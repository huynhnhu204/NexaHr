package com.nexahr.repository;

import com.nexahr.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("SELECT a FROM ActivityLog a WHERE " +
           "(:search IS NULL OR LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.user.username) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:action IS NULL OR a.action = :action) " +
           "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findWithFilters(@Param("search") String search,
                                        @Param("action") String action,
                                        Pageable pageable);
}
