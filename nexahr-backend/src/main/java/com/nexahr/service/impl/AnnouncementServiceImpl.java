package com.nexahr.service.impl;

import com.nexahr.dto.request.AnnouncementRequest;
import com.nexahr.dto.response.AnnouncementResponse;
import com.nexahr.entity.Announcement;
import com.nexahr.entity.Company;
import com.nexahr.entity.User;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.AnnouncementRepository;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.UserRepository;
import com.nexahr.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    public Page<AnnouncementResponse> list(Long companyId, Pageable pageable) {
        return announcementRepository
                .findByCompanyIdAndPublishedTrueOrderByPinnedDescCreatedAtDesc(companyId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public AnnouncementResponse create(Long companyId, Long authorId, AnnouncementRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        User author = userRepository.findById(authorId).orElse(null);

        Announcement announcement = Announcement.builder()
                .company(company)
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .pinned(request.getPinned() != null && request.getPinned())
                .published(request.getPublished() == null || request.getPublished())
                .build();
        return toResponse(announcementRepository.save(announcement));
    }

    @Override
    @Transactional
    public void delete(Long companyId, Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
        if (!a.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy thông báo");
        }
        announcementRepository.delete(a);
    }

    private AnnouncementResponse toResponse(Announcement a) {
        String authorName = a.getAuthor() != null ? a.getAuthor().getUsername() : "Hệ thống";
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .authorName(authorName)
                .pinned(a.isPinned())
                .published(a.isPublished())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
