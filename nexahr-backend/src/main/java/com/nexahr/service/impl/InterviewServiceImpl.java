package com.nexahr.service.impl;

import com.nexahr.dto.request.InterviewRequest;
import com.nexahr.dto.response.InterviewResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.Candidate;
import com.nexahr.entity.Employee;
import com.nexahr.entity.Interview;
import com.nexahr.entity.enums.InterviewMode;
import com.nexahr.entity.enums.InterviewStatus;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CandidateRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.repository.InterviewRepository;
import com.nexahr.service.InterviewService;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public PageResponse<InterviewResponse> getAll(Long candidateId, Long interviewerId, InterviewStatus status, Pageable pageable) {
        return PageUtil.toPageResponse(interviewRepository.findWithFilters(candidateId, interviewerId, status, pageable)
                .map(this::toResponse));
    }

    @Override
    public InterviewResponse getById(Long id) {
        return toResponse(interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch phỏng vấn")));
    }

    @Override
    @Transactional
    public InterviewResponse create(InterviewRequest request) {
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên"));
        Employee interviewer = employeeRepository.findById(request.getInterviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người phỏng vấn"));

        Interview interview = Interview.builder()
                .candidate(candidate)
                .interviewer(interviewer)
                .scheduledAt(request.getScheduledAt())
                .duration(request.getDuration() != null ? request.getDuration() : 60)
                .mode(request.getMode() != null ? request.getMode() : InterviewMode.ONLINE)
                .location(request.getLocation())
                .meetingLink(request.getMeetingLink())
                .status(request.getStatus() != null ? request.getStatus() : InterviewStatus.SCHEDULED)
                .evaluation(request.getEvaluation())
                .notes(request.getNotes())
                .build();

        return toResponse(interviewRepository.save(interview));
    }

    @Override
    @Transactional
    public InterviewResponse update(Long id, InterviewRequest request) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch phỏng vấn"));

        if (request.getCandidateId() != null) {
            interview.setCandidate(candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên")));
        }
        if (request.getInterviewerId() != null) {
            interview.setInterviewer(employeeRepository.findById(request.getInterviewerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người phỏng vấn")));
        }
        if (request.getScheduledAt() != null) interview.setScheduledAt(request.getScheduledAt());
        if (request.getDuration() != null) interview.setDuration(request.getDuration());
        if (request.getMode() != null) interview.setMode(request.getMode());
        if (request.getLocation() != null) interview.setLocation(request.getLocation());
        if (request.getMeetingLink() != null) interview.setMeetingLink(request.getMeetingLink());
        if (request.getStatus() != null) interview.setStatus(request.getStatus());
        if (request.getEvaluation() != null) interview.setEvaluation(request.getEvaluation());
        if (request.getNotes() != null) interview.setNotes(request.getNotes());

        return toResponse(interviewRepository.save(interview));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!interviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch phỏng vấn");
        }
        interviewRepository.deleteById(id);
    }

    private InterviewResponse toResponse(Interview interview) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .candidateId(interview.getCandidate().getId())
                .candidateName(interview.getCandidate().getFullName())
                .interviewerId(interview.getInterviewer().getId())
                .interviewerName(interview.getInterviewer().getFullName())
                .scheduledAt(interview.getScheduledAt())
                .duration(interview.getDuration())
                .mode(interview.getMode())
                .location(interview.getLocation())
                .meetingLink(interview.getMeetingLink())
                .status(interview.getStatus())
                .evaluation(interview.getEvaluation())
                .notes(interview.getNotes())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }
}
