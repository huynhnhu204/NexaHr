package com.nexahr.service.impl;

import com.nexahr.config.DefaultRolePermissions;
import com.nexahr.dto.request.CustomRoleRequest;
import com.nexahr.dto.response.CustomRoleResponse;
import com.nexahr.entity.*;
import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.*;
import com.nexahr.service.CustomRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomRoleServiceImpl implements CustomRoleService {

    private final CustomRoleRepository customRoleRepository;
    private final CustomRolePermissionRepository customRolePermissionRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomRoleResponse> list(Long companyId) {
        return customRoleRepository.findByCompanyIdOrderByNameAsc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CustomRoleResponse create(Long companyId, CustomRoleRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if (customRoleRepository.existsByCompanyIdAndCode(companyId, request.getCode().toUpperCase())) {
            throw new BadRequestException("Mã vai trò đã tồn tại");
        }

        CustomRole role = CustomRole.builder()
                .company(company)
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .baseRole(request.getBaseRole())
                .active(request.getActive() == null || request.getActive())
                .build();
        role = customRoleRepository.save(role);
        applyPermissions(role, request);
        return toResponse(customRoleRepository.findById(role.getId()).orElse(role));
    }

    @Override
    @Transactional
    public CustomRoleResponse update(Long companyId, Long id, CustomRoleRequest request) {
        CustomRole role = findRole(companyId, id);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setBaseRole(request.getBaseRole());
        if (request.getActive() != null) {
            role.setActive(request.getActive());
        }
        role.getPermissions().clear();
        customRoleRepository.save(role);
        applyPermissions(role, request);
        return toResponse(customRoleRepository.findById(id).orElse(role));
    }

    @Override
    @Transactional
    public void delete(Long companyId, Long id) {
        CustomRole role = findRole(companyId, id);
        long assigned = userRepository.countByCustomRoleId(role.getId());
        if (assigned > 0) {
            throw new BadRequestException("Không thể xóa vai trò đang được gán cho " + assigned + " người dùng");
        }
        customRoleRepository.delete(role);
    }

    @Override
    @Transactional
    public void assignToUser(Long companyId, Long userId, Long customRoleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("User chưa có hồ sơ nhân viên"));
        if (!employee.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Người dùng không thuộc công ty hiện tại");
        }

        if (customRoleId == null) {
            user.setCustomRole(null);
        } else {
            CustomRole role = findRole(companyId, customRoleId);
            user.setCustomRole(role);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void seedDemoRoles(Company company) {
        if (!customRoleRepository.findByCompanyIdOrderByNameAsc(company.getId()).isEmpty()) {
            return;
        }
        CustomRoleRequest payrollSpecialist = new CustomRoleRequest();
        payrollSpecialist.setName("Chuyên viên Payroll");
        payrollSpecialist.setCode("PAYROLL_SPEC");
        payrollSpecialist.setDescription("Xem và quản lý bảng lương, không có quyền HR đầy đủ");
        payrollSpecialist.setBaseRole(Role.EMPLOYEE);
        payrollSpecialist.setActive(true);
        payrollSpecialist.setPermissions(Arrays.stream(PermissionCode.values())
                .map(code -> {
                    CustomRoleRequest.PermissionGrant grant = new CustomRoleRequest.PermissionGrant();
                    grant.setPermission(code);
                    grant.setGranted(code == PermissionCode.PAYROLL_VIEW_ALL || code == PermissionCode.PAYROLL_MANAGE);
                    return grant;
                }).toList());
        create(company.getId(), payrollSpecialist);
    }

    private void applyPermissions(CustomRole role, CustomRoleRequest request) {
        if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
            for (PermissionCode code : PermissionCode.values()) {
                boolean granted = DefaultRolePermissions.isGrantedByDefault(role.getBaseRole(), code);
                customRolePermissionRepository.save(CustomRolePermission.builder()
                        .customRole(role)
                        .permission(code)
                        .granted(granted)
                        .build());
            }
            return;
        }
        for (CustomRoleRequest.PermissionGrant grant : request.getPermissions()) {
            customRolePermissionRepository.save(CustomRolePermission.builder()
                    .customRole(role)
                    .permission(grant.getPermission())
                    .granted(grant.getGranted() == null || grant.getGranted())
                    .build());
        }
    }

    private CustomRole findRole(Long companyId, Long id) {
        return customRoleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Custom role not found"));
    }

    private CustomRoleResponse toResponse(CustomRole role) {
        List<CustomRoleResponse.PermissionGrant> grants = role.getPermissions().stream()
                .sorted(Comparator.comparing(g -> g.getPermission().name()))
                .map(p -> CustomRoleResponse.PermissionGrant.builder()
                        .permission(p.getPermission())
                        .granted(p.isGranted())
                        .build())
                .toList();

        return CustomRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .description(role.getDescription())
                .baseRole(role.getBaseRole())
                .active(role.isActive())
                .permissions(grants)
                .assignedUsers((int) userRepository.countByCustomRoleId(role.getId()))
                .createdAt(role.getCreatedAt())
                .build();
    }
}
