package com.nexahr.service.impl;

import com.nexahr.dto.response.AttendanceResponse;
import com.nexahr.dto.response.CompanyAttendanceLocationResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.Attendance;
import com.nexahr.entity.Company;
import com.nexahr.entity.Employee;
import com.nexahr.entity.enums.AttendanceStatus;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.mapper.EmployeeMapper;
import com.nexahr.repository.AttendanceRepository;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.service.FileStorageService;
import com.nexahr.tenant.TenantContext;
import com.nexahr.util.GeoUtils;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeMapper mapper;
    private final FileStorageService fileStorageService;

    private static final LocalTime WORK_START = LocalTime.of(8, 30);
    private static final LocalTime WORK_END = LocalTime.of(17, 30);
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Transactional
    public AttendanceResponse checkIn(Long employeeId, MultipartFile photo, Double latitude, Double longitude, String address, String note) {
        Employee employee = requireActiveEmployee(employeeId);
        Company company = requireCompanyWithLocation(employee.getCompany().getId());
        validateCoordinates(latitude, longitude);

        LocalDate today = LocalDate.now();
        if (attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today).isPresent()) {
            throw new BadRequestException("Bạn đã chấm công vào hôm nay rồi");
        }

        double distance = GeoUtils.calculateDistanceMeters(
                latitude, longitude, company.getLatitude(), company.getLongitude());
        int radius = company.getAttendanceRadiusMeters() != null ? company.getAttendanceRadiusMeters() : 300;
        if (distance > radius) {
            throw new BadRequestException("Bạn đang ở ngoài phạm vi chấm công.");
        }

        LocalDateTime now = LocalDateTime.now();
        String storedPath = storeAttendancePhoto(photo, company.getId(), employeeId, "checkin", now);
        AttendanceStatus status = now.toLocalTime().isAfter(WORK_START) ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(today)
                .checkInTime(now)
                .checkInPhotoUrl(toPublicUrl(storedPath))
                .checkInLatitude(latitude)
                .checkInLongitude(longitude)
                .checkInAddress(address)
                .checkInDistanceMeters(distance)
                .status(status)
                .note(note)
                .build();
        return mapper.toAttendanceResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse checkOut(Long employeeId, MultipartFile photo, Double latitude, Double longitude, String address, String note) {
        Employee employee = requireActiveEmployee(employeeId);
        Company company = requireCompanyWithLocation(employee.getCompany().getId());
        validateCoordinates(latitude, longitude);

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("Chưa chấm công vào hôm nay"));

        if (attendance.getCheckOutTime() != null) {
            throw new BadRequestException("Bạn đã chấm công ra hôm nay rồi");
        }

        double distance = GeoUtils.calculateDistanceMeters(
                latitude, longitude, company.getLatitude(), company.getLongitude());
        int radius = company.getAttendanceRadiusMeters() != null ? company.getAttendanceRadiusMeters() : 300;
        if (distance > radius) {
            throw new BadRequestException("Bạn đang ở ngoài phạm vi chấm công.");
        }

        LocalDateTime now = LocalDateTime.now();
        String storedPath = storeAttendancePhoto(photo, company.getId(), employeeId, "checkout", now);
        attendance.setCheckOutTime(now);
        attendance.setCheckOutPhotoUrl(toPublicUrl(storedPath));
        attendance.setCheckOutLatitude(latitude);
        attendance.setCheckOutLongitude(longitude);
        attendance.setCheckOutAddress(address);
        attendance.setCheckOutDistanceMeters(distance);
        if (note != null && !note.isBlank()) {
            attendance.setNote(note);
        }

        long minutes = ChronoUnit.MINUTES.between(attendance.getCheckInTime(), now);
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        attendance.setTotalHours(hours);

        if (now.toLocalTime().isBefore(WORK_END) && attendance.getStatus() != AttendanceStatus.LATE) {
            attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
        }

        return mapper.toAttendanceResponse(attendanceRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getToday(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .map(mapper::toAttendanceResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getAll(Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return PageUtil.toPageResponse(attendanceRepository.findWithFilters(requireCompanyId(), employeeId, startDate, endDate, pageable)
                .map(mapper::toAttendanceResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getMyAttendance(Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return getAll(employeeId, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public CompanyAttendanceLocationResponse getCompanyAttendanceLocation(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        boolean configured = company.getLatitude() != null && company.getLongitude() != null;
        return CompanyAttendanceLocationResponse.builder()
                .latitude(company.getLatitude())
                .longitude(company.getLongitude())
                .radiusMeters(company.getAttendanceRadiusMeters() != null ? company.getAttendanceRadiusMeters() : 300)
                .address(company.getAddress())
                .configured(configured)
                .build();
    }

    public Long resolveEmployeeId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .map(Employee::getId)
                .orElse(null);
    }

    private Employee requireActiveEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));
        if (employee.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new BadRequestException("Nhân viên không ở trạng thái hoạt động");
        }
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null && !employee.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Nhân viên không thuộc công ty hiện tại");
        }
        return employee;
    }

    private Company requireCompanyWithLocation(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        if (company.getLatitude() == null || company.getLongitude() == null) {
            throw new BadRequestException("Chưa cấu hình vị trí công ty. Vui lòng liên hệ quản trị viên.");
        }
        return company;
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BadRequestException("Bắt buộc có vị trí (latitude/longitude)");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new BadRequestException("Tọa độ vị trí không hợp lệ");
        }
    }

    private String storeAttendancePhoto(MultipartFile photo, Long companyId, Long employeeId, String prefix, LocalDateTime when) {
        String subDir = "attendance/" + companyId + "/" + employeeId;
        String ext = extensionFor(photo);
        String fileName = prefix + "_" + when.format(FILE_TS) + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        return fileStorageService.storeWithFileName(photo, subDir, fileName);
    }

    private String extensionFor(MultipartFile photo) {
        String contentType = photo.getContentType();
        if (contentType == null) return ".jpg";
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String toPublicUrl(String relativePath) {
        return "/uploads/" + relativePath;
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
