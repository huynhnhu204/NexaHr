package com.nexahr.service;

import com.nexahr.dto.request.InterviewRequest;
import com.nexahr.dto.response.InterviewResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.enums.InterviewStatus;
import org.springframework.data.domain.Pageable;

public interface InterviewService {
    PageResponse<InterviewResponse> getAll(Long candidateId, Long interviewerId, InterviewStatus status, Pageable pageable);
    InterviewResponse getById(Long id);
    InterviewResponse create(InterviewRequest request);
    InterviewResponse update(Long id, InterviewRequest request);
    void delete(Long id);
}
