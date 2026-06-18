package com.nexahr.service.impl;

import com.nexahr.dto.request.PublicApplyRequest;
import com.nexahr.dto.response.PublicCompanyResponse;
import com.nexahr.dto.response.PublicJobDetailResponse;
import com.nexahr.dto.response.PublicJobResponse;
import com.nexahr.entity.Candidate;
import com.nexahr.entity.Company;
import com.nexahr.entity.JobPosting;
import com.nexahr.entity.enums.CandidateStatus;
import com.nexahr.entity.enums.JobStatus;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CandidateRepository;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.JobPostingRepository;
import com.nexahr.service.PublicCareersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCareersServiceImpl implements PublicCareersService {

    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final CompanyRepository companyRepository;

    @Override
    public PublicCompanyResponse getCompanyInfo(String companyCode) {
        Company company = companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        return PublicCompanyResponse.builder()
                .name(company.getName())
                .code(company.getCode())
                .logo(company.getLogo())
                .website(company.getWebsite())
                .primaryColor(company.getPrimaryColor())
                .careersTagline(company.getCareersTagline())
                .address(company.getAddress())
                .build();
    }

    @Override
    public List<PublicJobResponse> listJobs(String companyCode) {
        return jobPostingRepository
                .findByCompany_CodeAndStatusAndPublishedToCareersTrue(companyCode, JobStatus.OPEN)
                .stream()
                .map(this::toPublicJobResponse)
                .toList();
    }

    @Override
    public PublicJobDetailResponse getJob(String companyCode, Long jobId) {
        JobPosting job = jobPostingRepository
                .findByIdAndCompany_CodeAndPublishedToCareersTrue(jobId, companyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));
        return toPublicJobDetailResponse(job);
    }

    @Override
    @Transactional
    public void apply(String companyCode, Long jobId, PublicApplyRequest request) {
        JobPosting job = jobPostingRepository
                .findByIdAndCompany_CodeAndPublishedToCareersTrue(jobId, companyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new ResourceNotFoundException("Tin tuyển dụng không còn mở");
        }

        Candidate candidate = Candidate.builder()
                .jobPosting(job)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(CandidateStatus.NEW)
                .note(request.getCvNote())
                .build();
        candidateRepository.save(candidate);
    }

    private PublicJobResponse toPublicJobResponse(JobPosting job) {
        return PublicJobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .departmentName(job.getDepartment() != null ? job.getDepartment().getName() : null)
                .positionName(job.getPosition() != null ? job.getPosition().getName() : null)
                .salaryRange(job.getSalaryRange())
                .createdAt(job.getCreatedAt())
                .build();
    }

    private PublicJobDetailResponse toPublicJobDetailResponse(JobPosting job) {
        return PublicJobDetailResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .departmentName(job.getDepartment() != null ? job.getDepartment().getName() : null)
                .positionName(job.getPosition() != null ? job.getPosition().getName() : null)
                .description(job.getDescription())
                .requirement(job.getRequirement())
                .salaryRange(job.getSalaryRange())
                .createdAt(job.getCreatedAt())
                .build();
    }
}
