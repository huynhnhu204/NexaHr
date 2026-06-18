package com.nexahr.service.impl;

import com.nexahr.dto.request.CourseRequest;
import com.nexahr.dto.response.CourseResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.TrainingEnrollmentResponse;
import com.nexahr.entity.Course;
import com.nexahr.entity.Employee;
import com.nexahr.entity.TrainingEnrollment;
import com.nexahr.entity.enums.CourseStatus;
import com.nexahr.entity.enums.EnrollmentStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CourseRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.repository.TrainingEnrollmentRepository;
import com.nexahr.service.TrainingService;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final CourseRepository courseRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public PageResponse<CourseResponse> getCourses(String search, CourseStatus status, Pageable pageable) {
        return PageUtil.toPageResponse(courseRepository.findWithFilters(search, status, pageable)
                .map(this::toCourseResponse));
    }

    @Override
    public CourseResponse getCourseById(Long id) {
        return toCourseResponse(courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học")));
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructor(request.getInstructor())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxParticipants(request.getMaxParticipants())
                .status(request.getStatus() != null ? request.getStatus() : CourseStatus.ACTIVE)
                .build();
        return toCourseResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setInstructor(request.getInstructor());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setMaxParticipants(request.getMaxParticipants());
        if (request.getStatus() != null) course.setStatus(request.getStatus());

        return toCourseResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy khóa học");
        }
        courseRepository.deleteById(id);
    }

    @Override
    public PageResponse<TrainingEnrollmentResponse> getEnrollments(Long courseId, Long employeeId, EnrollmentStatus status, Pageable pageable) {
        return PageUtil.toPageResponse(enrollmentRepository.findWithFilters(courseId, employeeId, status, pageable)
                .map(this::toEnrollmentResponse));
    }

    @Override
    @Transactional
    public TrainingEnrollmentResponse enroll(Long courseId, Long employeeId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new BadRequestException("Khóa học không còn hoạt động");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        if (enrollmentRepository.findByCourseIdAndEmployeeId(courseId, employeeId).isPresent()) {
            throw new BadRequestException("Nhân viên đã đăng ký khóa học này");
        }

        if (course.getMaxParticipants() != null) {
            long current = enrollmentRepository.findWithFilters(courseId, null, null,
                    org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
            if (current >= course.getMaxParticipants()) {
                throw new BadRequestException("Khóa học đã đủ số lượng học viên");
            }
        }

        TrainingEnrollment enrollment = TrainingEnrollment.builder()
                .course(course)
                .employee(employee)
                .enrolledAt(LocalDateTime.now())
                .status(EnrollmentStatus.ENROLLED)
                .build();

        return toEnrollmentResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public TrainingEnrollmentResponse updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status, Integer score) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học"));

        enrollment.setStatus(status);
        if (score != null) enrollment.setScore(score);
        if (status == EnrollmentStatus.COMPLETED) {
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        return toEnrollmentResponse(enrollmentRepository.save(enrollment));
    }

    private CourseResponse toCourseResponse(Course course) {
        long enrollmentCount = enrollmentRepository.findWithFilters(course.getId(), null, null,
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructor(course.getInstructor())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .maxParticipants(course.getMaxParticipants())
                .status(course.getStatus())
                .enrollmentCount((int) enrollmentCount)
                .createdAt(course.getCreatedAt())
                .build();
    }

    private TrainingEnrollmentResponse toEnrollmentResponse(TrainingEnrollment enrollment) {
        return TrainingEnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .employeeId(enrollment.getEmployee().getId())
                .employeeName(enrollment.getEmployee().getFullName())
                .enrolledAt(enrollment.getEnrolledAt())
                .status(enrollment.getStatus())
                .completedAt(enrollment.getCompletedAt())
                .score(enrollment.getScore())
                .build();
    }
}
