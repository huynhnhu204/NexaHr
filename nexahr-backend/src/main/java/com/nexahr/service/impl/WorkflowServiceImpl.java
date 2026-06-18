package com.nexahr.service.impl;

import com.nexahr.dto.request.WorkflowRuleRequest;
import com.nexahr.dto.response.WorkflowRuleResponse;
import com.nexahr.entity.*;
import com.nexahr.entity.enums.*;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.repository.LeaveRepository;
import com.nexahr.repository.WorkflowRuleRepository;
import com.nexahr.service.EmailService;
import com.nexahr.service.NotificationDispatchService;
import com.nexahr.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRuleRepository workflowRuleRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final NotificationDispatchService notificationDispatchService;
    private final EmailService emailService;

    @Override
    public List<WorkflowRuleResponse> list(Long companyId) {
        return workflowRuleRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowRuleResponse create(Long companyId, WorkflowRuleRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        WorkflowRule rule = WorkflowRule.builder()
                .company(company)
                .name(request.getName())
                .trigger(request.getTrigger())
                .action(request.getAction())
                .configValue(request.getConfigValue())
                .active(request.getActive() == null || request.getActive())
                .build();

        return toResponse(workflowRuleRepository.save(rule));
    }

    @Override
    @Transactional
    public WorkflowRuleResponse update(Long companyId, Long id, WorkflowRuleRequest request) {
        WorkflowRule rule = findRule(companyId, id);
        rule.setName(request.getName());
        rule.setTrigger(request.getTrigger());
        rule.setAction(request.getAction());
        rule.setConfigValue(request.getConfigValue());
        if (request.getActive() != null) {
            rule.setActive(request.getActive());
        }
        return toResponse(workflowRuleRepository.save(rule));
    }

    @Override
    @Transactional
    public void delete(Long companyId, Long id) {
        workflowRuleRepository.delete(findRule(companyId, id));
    }

    @Override
    @Transactional
    public void onLeaveCreated(LeaveRequest leave) {
        Long companyId = leave.getEmployee().getCompany().getId();
        List<WorkflowRule> rules = workflowRuleRepository
                .findByCompanyIdAndTriggerAndActiveTrue(companyId, WorkflowTrigger.LEAVE_CREATED);

        if (rules.isEmpty()) {
            return;
        }

        boolean autoApproved = false;
        for (WorkflowRule rule : rules) {
            switch (rule.getAction()) {
                case NOTIFY_MANAGER -> notifyManager(leave);
                case NOTIFY_HR -> notifyHr(companyId, leave);
                case AUTO_APPROVE_LEAVE_DAYS_LTE -> {
                    if (!autoApproved && shouldAutoApprove(leave, rule)) {
                        autoApproveLeave(leave);
                        autoApproved = true;
                    }
                }
                case SEND_EMAIL -> sendLeaveEmail(leave);
                default -> log.debug("Unhandled workflow action: {}", rule.getAction());
            }
        }
    }

    private void notifyManager(LeaveRequest leave) {
        Employee manager = leave.getEmployee().getManager();
        if (manager == null || manager.getUser() == null) {
            return;
        }
        String title = "Đơn nghỉ phép mới cần duyệt";
        String message = String.format("%s gửi đơn nghỉ từ %s đến %s (%d ngày).",
                leave.getEmployee().getFullName(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getTotalDays());
        notificationDispatchService.notify(manager.getUser(), title, message, NotificationType.GENERAL);
    }

    private void notifyHr(Long companyId, LeaveRequest leave) {
        List<User> hrUsers = findUsersByRole(companyId, Role.HR);
        String title = "Đơn nghỉ phép mới";
        String message = String.format("%s — %s đến %s",
                leave.getEmployee().getFullName(), leave.getStartDate(), leave.getEndDate());
        for (User hr : hrUsers) {
            notificationDispatchService.notify(hr, title, message, NotificationType.GENERAL);
        }
    }

    private void sendLeaveEmail(LeaveRequest leave) {
        Employee manager = leave.getEmployee().getManager();
        if (manager == null || manager.getUser() == null) {
            return;
        }
        emailService.sendNotificationEmail(
                manager.getUser().getEmail(),
                "NexaHR - Đơn nghỉ phép mới",
                String.format("Nhân viên %s gửi đơn nghỉ từ %s đến %s.",
                        leave.getEmployee().getFullName(), leave.getStartDate(), leave.getEndDate())
        );
    }

    private boolean shouldAutoApprove(LeaveRequest leave, WorkflowRule rule) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            return false;
        }
        int maxDays = parseConfigDays(rule.getConfigValue(), 2);
        return leave.getTotalDays() <= maxDays;
    }

    private void autoApproveLeave(LeaveRequest leave) {
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedAt(LocalDateTime.now());
        leaveRepository.save(leave);

        if (leave.getLeaveType() == LeaveType.ANNUAL_LEAVE) {
            Employee emp = leave.getEmployee();
            emp.setAnnualLeaveBalance(emp.getAnnualLeaveBalance() - leave.getTotalDays());
            employeeRepository.save(emp);
        }

        if (leave.getEmployee().getUser() != null) {
            notificationDispatchService.notify(
                    leave.getEmployee().getUser(),
                    "Đơn nghỉ phép tự động duyệt",
                    String.format("Đơn nghỉ từ %s đến %s đã được duyệt tự động theo quy trình.",
                            leave.getStartDate(), leave.getEndDate()),
                    NotificationType.LEAVE_APPROVED
            );
        }
        log.info("Workflow auto-approved leave id={} days={}", leave.getId(), leave.getTotalDays());
    }

    private List<User> findUsersByRole(Long companyId, Role role) {
        return employeeRepository.findByCompanyIdAndUserRole(companyId, role).stream()
                .map(Employee::getUser)
                .filter(user -> user != null)
                .toList();
    }

    private int parseConfigDays(String config, int defaultDays) {
        if (config == null || config.isBlank()) {
            return defaultDays;
        }
        try {
            return Integer.parseInt(config.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("configValue phải là số ngày hợp lệ");
        }
    }

    private WorkflowRule findRule(Long companyId, Long id) {
        WorkflowRule rule = workflowRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow rule not found"));
        if (!rule.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Workflow rule not found");
        }
        return rule;
    }

    private WorkflowRuleResponse toResponse(WorkflowRule rule) {
        return WorkflowRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .trigger(rule.getTrigger())
                .action(rule.getAction())
                .configValue(rule.getConfigValue())
                .active(rule.isActive())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
