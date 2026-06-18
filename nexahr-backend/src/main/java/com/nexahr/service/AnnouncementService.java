package com.nexahr.service;

import com.nexahr.dto.request.AnnouncementRequest;
import com.nexahr.dto.response.AnnouncementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnnouncementService {
    Page<AnnouncementResponse> list(Long companyId, Pageable pageable);
    AnnouncementResponse create(Long companyId, Long authorId, AnnouncementRequest request);
    void delete(Long companyId, Long id);
}
