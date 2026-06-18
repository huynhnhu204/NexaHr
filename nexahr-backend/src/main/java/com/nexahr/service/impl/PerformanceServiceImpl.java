package com.nexahr.service.impl;

import com.nexahr.dto.request.PerformanceFinalizeRequest;
import com.nexahr.dto.request.PerformanceReviewRequest;
import com.nexahr.dto.request.PerformanceSelfReviewRequest;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.PerformanceReviewResponse;
import com.nexahr.entity.Employee;
import com.nexahr.entity.PerformanceReview;
import com.nexahr.entity.enums.PerformanceReviewStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.repository.PerformanceReviewRepository;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl {

    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeRepository employeeRepository;

    public PageResponse<PerformanceReviewResponse> getAll(Pageable pageable) {
        return PageUtil.toPageResponse(reviewRepository.findAll(pageable).map(this::toResponse));
    }

    public PageResponse<PerformanceReviewResponse> getMy(Long employeeId, Pageable pageable) {
        return PageUtil.toPageResponse(reviewRepository.findByEmployeeId(employeeId, pageable).map(this::toResponse));
    }

    public PageResponse<PerformanceReviewResponse> getByEmployee(Long employeeId, Pageable pageable) {
        return PageUtil.toPageResponse(reviewRepository.findByEmployeeId(employeeId, pageable).map(this::toResponse));
    }

    @Transactional
    public PerformanceReviewResponse create(Long reviewerId, PerformanceReviewRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .reviewer(reviewer)
                .reviewPeriod(request.getReviewPeriod())
                .goals(request.getGoals())
                .dueDate(request.getDueDate())
                .score(request.getScore())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(PerformanceReviewStatus.DRAFT)
                .build();
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public PerformanceReviewResponse publish(Long id) {
        PerformanceReview review = findReview(id);
        if (review.getStatus() != PerformanceReviewStatus.DRAFT) {
            throw new BadRequestException("Chỉ có thể gửi đánh giá ở trạng thái nháp");
        }
        review.setStatus(PerformanceReviewStatus.PENDING_SELF);
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public PerformanceReviewResponse submitSelfReview(Long id, Long employeeId, PerformanceSelfReviewRequest request) {
        PerformanceReview review = findReview(id);
        if (!review.getEmployee().getId().equals(employeeId)) {
            throw new BadRequestException("Không có quyền gửi đánh giá này");
        }
        if (review.getStatus() != PerformanceReviewStatus.PENDING_SELF) {
            throw new BadRequestException("Đánh giá không ở giai đoạn tự đánh giá");
        }
        review.setEmployeeSelfComment(request.getEmployeeSelfComment());
        review.setStatus(PerformanceReviewStatus.PENDING_MANAGER);
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public PerformanceReviewResponse finalizeReview(Long id, Long reviewerId, PerformanceFinalizeRequest request) {
        PerformanceReview review = findReview(id);
        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new BadRequestException("Chỉ người đánh giá được phân công mới có thể hoàn tất");
        }
        if (review.getStatus() != PerformanceReviewStatus.PENDING_MANAGER) {
            throw new BadRequestException("Đánh giá chưa sẵn sàng để quản lý chấm điểm");
        }
        review.setScore(request.getScore());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(PerformanceReviewStatus.COMPLETED);
        return toResponse(reviewRepository.save(review));
    }

    private PerformanceReview findReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found"));
    }

    private PerformanceReviewResponse toResponse(PerformanceReview r) {
        return PerformanceReviewResponse.builder()
                .id(r.getId())
                .employeeId(r.getEmployee().getId())
                .employeeName(r.getEmployee().getFullName())
                .reviewerId(r.getReviewer().getId())
                .reviewerName(r.getReviewer().getFullName())
                .reviewPeriod(r.getReviewPeriod())
                .goals(r.getGoals())
                .dueDate(r.getDueDate())
                .status(r.getStatus())
                .employeeSelfComment(r.getEmployeeSelfComment())
                .score(r.getScore())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
