package com.nexahr.controller;

import com.nexahr.dto.request.CourseRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.CourseResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.TrainingEnrollmentResponse;
import com.nexahr.entity.enums.CourseStatus;
import com.nexahr.entity.enums.EnrollmentStatus;
import com.nexahr.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping("/api/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<PageResponse<CourseResponse>> getCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(trainingService.getCourses(search, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/api/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        return ApiResponse.success(trainingService.getCourseById(id));
    }

    @PostMapping("/api/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ApiResponse.success("Tạo khóa học thành công", trainingService.createCourse(request));
    }

    @PutMapping("/api/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<CourseResponse> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ApiResponse.success("Cập nhật khóa học thành công", trainingService.updateCourse(id, request));
    }

    @DeleteMapping("/api/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        trainingService.deleteCourse(id);
        return ApiResponse.success("Xóa khóa học thành công", null);
    }

    @GetMapping("/api/training-enrollments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<PageResponse<TrainingEnrollmentResponse>> getEnrollments(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(trainingService.getEnrollments(courseId, employeeId, status,
                PageRequest.of(page, size, Sort.by("enrolledAt").descending())));
    }

    @PostMapping("/api/courses/{courseId}/enroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<TrainingEnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @RequestBody Map<String, Long> body) {
        return ApiResponse.success("Đăng ký khóa học thành công",
                trainingService.enroll(courseId, body.get("employeeId")));
    }

    @PutMapping("/api/training-enrollments/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<TrainingEnrollmentResponse> updateEnrollmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        EnrollmentStatus status = EnrollmentStatus.valueOf(body.get("status").toString());
        Integer score = body.get("score") != null ? Integer.valueOf(body.get("score").toString()) : null;
        return ApiResponse.success("Cập nhật trạng thái đăng ký thành công",
                trainingService.updateEnrollmentStatus(id, status, score));
    }
}
