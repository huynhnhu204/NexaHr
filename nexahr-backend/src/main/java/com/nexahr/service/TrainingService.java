package com.nexahr.service;

import com.nexahr.dto.request.CourseRequest;
import com.nexahr.dto.response.CourseResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.TrainingEnrollmentResponse;
import com.nexahr.entity.enums.CourseStatus;
import com.nexahr.entity.enums.EnrollmentStatus;
import org.springframework.data.domain.Pageable;

public interface TrainingService {
    PageResponse<CourseResponse> getCourses(String search, CourseStatus status, Pageable pageable);
    CourseResponse getCourseById(Long id);
    CourseResponse createCourse(CourseRequest request);
    CourseResponse updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
    PageResponse<TrainingEnrollmentResponse> getEnrollments(Long courseId, Long employeeId, EnrollmentStatus status, Pageable pageable);
    TrainingEnrollmentResponse enroll(Long courseId, Long employeeId);
    TrainingEnrollmentResponse updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status, Integer score);
}
