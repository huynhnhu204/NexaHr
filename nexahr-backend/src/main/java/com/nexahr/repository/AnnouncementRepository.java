package com.nexahr.repository;

import com.nexahr.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Page<Announcement> findByCompanyIdAndPublishedTrueOrderByPinnedDescCreatedAtDesc(Long companyId, Pageable pageable);
}
