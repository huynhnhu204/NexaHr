package com.nexahr.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionCode {
    EMPLOYEE_VIEW("Xem nhân viên"),
    EMPLOYEE_MANAGE("Quản lý nhân viên"),
    DEPARTMENT_MANAGE("Quản lý phòng ban"),
    PAYROLL_VIEW_ALL("Xem bảng lương toàn công ty"),
    PAYROLL_MANAGE("Quản lý bảng lương"),
    LEAVE_APPROVE("Duyệt nghỉ phép"),
    LEAVE_VIEW_ALL("Xem đơn nghỉ phép"),
    RECRUITMENT_MANAGE("Quản lý tuyển dụng"),
    ANALYTICS_VIEW("Xem phân tích HR"),
    SETTINGS_MANAGE("Cài đặt công ty"),
    INTEGRATIONS_MANAGE("Quản lý tích hợp"),
    WORKFLOW_MANAGE("Quản lý quy trình"),
    AUDIT_VIEW("Xem nhật ký bảo mật"),
    PERMISSIONS_MANAGE("Quản lý phân quyền");

    private final String label;
}
