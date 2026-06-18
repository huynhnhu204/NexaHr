package com.nexahr.service.impl;

import com.nexahr.dto.response.MobileSummaryResponse;
import com.nexahr.entity.Attendance;
import com.nexahr.entity.Employee;
import com.nexahr.entity.enums.LeaveStatus;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.AttendanceRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.repository.LeaveRepository;
import com.nexahr.repository.NotificationRepository;
import com.nexahr.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MobileServiceImpl implements MobileService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public MobileSummaryResponse getSummary(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), today).orElse(null);

        return MobileSummaryResponse.builder()
                .fullName(employee.getFullName())
                .checkedInToday(attendance != null && attendance.getCheckInTime() != null)
                .checkedOutToday(attendance != null && attendance.getCheckOutTime() != null)
                .todayAttendanceStatus(attendance != null && attendance.getStatus() != null
                        ? attendance.getStatus().name() : null)
                .pendingLeaves(leaveRepository.countByStatusAndCompanyId(LeaveStatus.PENDING, employee.getCompany().getId()))
                .unreadNotifications(notificationRepository.countByUserIdAndIsReadFalse(userId))
                .role(employee.getUser() != null && employee.getUser().getRole() != null
                        ? employee.getUser().getRole().name() : null)
                .companyName(employee.getCompany() != null ? employee.getCompany().getName() : null)
                .build();
    }
}
