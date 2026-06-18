package com.nexahr.service.impl;

import com.nexahr.dto.request.LeaveRequest;
import com.nexahr.dto.response.LeaveResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.Employee;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.LeaveStatus;
import com.nexahr.entity.enums.LeaveType;
import com.nexahr.entity.enums.NotificationType;
import com.nexahr.entity.enums.WebhookEvent;
import com.nexahr.service.WebhookService;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.mapper.EmployeeMapper;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.repository.LeaveRepository;
import com.nexahr.tenant.TenantContext;
import com.nexahr.util.PageUtil;
import com.nexahr.service.EmailService;
import com.nexahr.service.NotificationDispatchService;
import com.nexahr.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper mapper;
    private final EmailService emailService;
    private final WebhookService webhookService;
    private final NotificationDispatchService notificationDispatchService;
    private final WorkflowService workflowService;

    @Transactional
    public LeaveResponse create(Long employeeId, LeaveRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        if (request.getLeaveType() == LeaveType.ANNUAL_LEAVE && employee.getAnnualLeaveBalance() < totalDays) {
            throw new BadRequestException("Insufficient annual leave balance");
        }

        com.nexahr.entity.LeaveRequest leave = com.nexahr.entity.LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        leave = leaveRepository.save(leave);
        workflowService.onLeaveCreated(leave);
        return mapper.toLeaveResponse(leaveRepository.findById(leave.getId()).orElse(leave));
    }

    public PageResponse<LeaveResponse> getAll(LeaveStatus status, Long employeeId, Pageable pageable) {
        return PageUtil.toPageResponse(leaveRepository.findWithFilters(requireCompanyId(), status, employeeId, pageable)
                .map(mapper::toLeaveResponse));
    }

    public PageResponse<LeaveResponse> getMy(Long employeeId, Pageable pageable) {
        return PageUtil.toPageResponse(leaveRepository.findByEmployeeId(employeeId, pageable)
                .map(mapper::toLeaveResponse));
    }

    @Transactional
    public LeaveResponse approve(Long id, User approver) {
        com.nexahr.entity.LeaveRequest leave = leaveRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn nghỉ phép"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể duyệt đơn đang chờ xử lý");
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);
        leave.setApprovedAt(LocalDateTime.now());

        if (leave.getLeaveType() == LeaveType.ANNUAL_LEAVE) {
            Employee emp = leave.getEmployee();
            emp.setAnnualLeaveBalance(emp.getAnnualLeaveBalance() - leave.getTotalDays());
        }

        leaveRepository.save(leave);
        sendNotification(leave.getEmployee().getUser(), "Leave Approved",
                "Your leave request from " + leave.getStartDate() + " to " + leave.getEndDate() + " has been approved.",
                NotificationType.LEAVE_APPROVED);

        if (leave.getEmployee().getUser() != null) {
            emailService.sendNotificationEmail(
                    leave.getEmployee().getUser().getEmail(),
                    "NexaHR - Đơn nghỉ phép đã được duyệt",
                    "Đơn nghỉ phép từ " + leave.getStartDate() + " đến " + leave.getEndDate() + " của bạn đã được phê duyệt.");
        }

        Long companyId = leave.getEmployee().getCompany() != null ? leave.getEmployee().getCompany().getId() : null;
        if (companyId != null) {
            webhookService.dispatch(companyId, WebhookEvent.LEAVE_APPROVED, java.util.Map.of(
                    "leaveId", leave.getId(),
                    "employeeId", leave.getEmployee().getId(),
                    "employeeName", leave.getEmployee().getFullName(),
                    "startDate", leave.getStartDate().toString(),
                    "endDate", leave.getEndDate().toString(),
                    "status", "APPROVED"
            ));
        }

        return mapper.toLeaveResponse(leave);
    }

    @Transactional
    public LeaveResponse reject(Long id, User approver, String reason) {
        com.nexahr.entity.LeaveRequest leave = leaveRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn nghỉ phép"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể từ chối đơn đang chờ xử lý");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);
        leave.setApprovedAt(LocalDateTime.now());
        leave.setRejectReason(reason);

        leaveRepository.save(leave);
        sendNotification(leave.getEmployee().getUser(), "Leave Rejected",
                "Your leave request has been rejected. Reason: " + reason,
                NotificationType.LEAVE_REJECTED);

        Long companyId = leave.getEmployee().getCompany() != null ? leave.getEmployee().getCompany().getId() : null;
        if (companyId != null) {
            webhookService.dispatch(companyId, WebhookEvent.LEAVE_REJECTED, java.util.Map.of(
                    "leaveId", leave.getId(),
                    "employeeId", leave.getEmployee().getId(),
                    "reason", reason != null ? reason : "",
                    "status", "REJECTED"
            ));
        }

        return mapper.toLeaveResponse(leave);
    }

    private void sendNotification(User user, String title, String message, NotificationType type) {
        notificationDispatchService.notify(user, title, message, type);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
