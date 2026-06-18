package com.nexahr.service.impl;

import com.nexahr.dto.request.CandidateRequest;
import com.nexahr.dto.request.JobPostingRequest;
import com.nexahr.dto.response.*;
import com.nexahr.entity.*;
import com.nexahr.entity.enums.CandidateStatus;
import com.nexahr.entity.enums.JobStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.*;
import com.nexahr.tenant.TenantContext;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentServiceImpl {

    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final CompanyRepository companyRepository;

    public PageResponse<JobPostingResponse> getJobs(Pageable pageable) {
        return PageUtil.toPageResponse(jobPostingRepository.findAll(pageable).map(this::toJobResponse));
    }

    @Transactional
    public JobPostingResponse createJob(JobPostingRequest request) {
        Long companyId = requireCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        Department dept = resolveDepartment(request.getDepartmentId(), companyId);
        Position pos = resolvePosition(request.getPositionId(), companyId);

        JobPosting job = JobPosting.builder()
                .title(request.getTitle())
                .company(company)
                .department(dept)
                .position(pos)
                .description(request.getDescription())
                .requirement(request.getRequirement())
                .salaryRange(request.getSalaryRange())
                .status(request.getStatus() != null ? request.getStatus() : JobStatus.OPEN)
                .publishedToCareers(Boolean.TRUE.equals(request.getPublishedToCareers()))
                .build();
        return toJobResponse(jobPostingRepository.save(job));
    }

    @Transactional
    public JobPostingResponse updateJob(Long id, JobPostingRequest request) {
        Long companyId = requireCompanyId();
        JobPosting job = jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));

        if (!job.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Tin tuyển dụng không thuộc công ty hiện tại");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirement(request.getRequirement());
        job.setSalaryRange(request.getSalaryRange());
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }
        if (request.getDepartmentId() != null) {
            job.setDepartment(resolveDepartment(request.getDepartmentId(), companyId));
        }
        if (request.getPositionId() != null) {
            job.setPosition(resolvePosition(request.getPositionId(), companyId));
        }
        if (request.getPublishedToCareers() != null) {
            job.setPublishedToCareers(request.getPublishedToCareers());
        }
        return toJobResponse(jobPostingRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id) {
        Long companyId = requireCompanyId();
        JobPosting job = jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));

        if (!job.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Tin tuyển dụng không thuộc công ty hiện tại");
        }
        jobPostingRepository.deleteById(id);
    }

    public PageResponse<CandidateResponse> getCandidates(Long jobId, CandidateStatus status, Pageable pageable) {
        return PageUtil.toPageResponse(candidateRepository.findWithFilters(jobId, status, pageable)
                .map(this::toCandidateResponse));
    }

    @Transactional
    public CandidateResponse createCandidate(CandidateRequest request) {
        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));

        Candidate candidate = Candidate.builder()
                .jobPosting(job)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .cvFile(request.getCvFile())
                .status(request.getStatus() != null ? request.getStatus() : CandidateStatus.NEW)
                .note(request.getNote())
                .build();
        return toCandidateResponse(candidateRepository.save(candidate));
    }

    @Transactional
    public CandidateResponse updateCandidateStatus(Long id, CandidateStatus status) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên"));
        candidate.setStatus(status);
        return toCandidateResponse(candidateRepository.save(candidate));
    }

    private Department resolveDepartment(Long departmentId, Long companyId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .filter(d -> d.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new BadRequestException("Phòng ban không thuộc công ty hiện tại"));
    }

    private Position resolvePosition(Long positionId, Long companyId) {
        if (positionId == null) {
            return null;
        }
        return positionRepository.findById(positionId)
                .filter(p -> p.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new BadRequestException("Chức vụ không thuộc công ty hiện tại"));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }

    private JobPostingResponse toJobResponse(JobPosting j) {
        return JobPostingResponse.builder()
                .id(j.getId())
                .title(j.getTitle())
                .departmentId(j.getDepartment() != null ? j.getDepartment().getId() : null)
                .departmentName(j.getDepartment() != null ? j.getDepartment().getName() : null)
                .positionId(j.getPosition() != null ? j.getPosition().getId() : null)
                .positionName(j.getPosition() != null ? j.getPosition().getName() : null)
                .description(j.getDescription())
                .requirement(j.getRequirement())
                .salaryRange(j.getSalaryRange())
                .status(j.getStatus())
                .publishedToCareers(j.isPublishedToCareers())
                .createdAt(j.getCreatedAt())
                .build();
    }

    private CandidateResponse toCandidateResponse(Candidate c) {
        return CandidateResponse.builder()
                .id(c.getId())
                .jobId(c.getJobPosting().getId())
                .jobTitle(c.getJobPosting().getTitle())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .cvFile(c.getCvFile())
                .status(c.getStatus())
                .note(c.getNote())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
