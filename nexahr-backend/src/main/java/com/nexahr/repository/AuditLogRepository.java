package com.nexahr.repository;

import com.nexahr.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:search IS NULL OR LOWER(a.details) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.user.username) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:action IS NULL OR a.action = :action) " +
           "AND (:entityType IS NULL OR a.entityType = :entityType)")
    Page<AuditLog> findWithFilters(@Param("search") String search,
                                   @Param("action") String action,
                                   @Param("entityType") String entityType,
                                   Pageable pageable);
}
