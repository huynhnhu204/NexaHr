package com.nexahr.service.impl;

import com.nexahr.config.DefaultRolePermissions;
import com.nexahr.dto.request.RolePermissionUpdateRequest;
import com.nexahr.dto.response.PermissionMatrixResponse;
import com.nexahr.dto.response.UserPermissionsResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.CustomRole;
import com.nexahr.entity.CustomRolePermission;
import com.nexahr.entity.RolePermission;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.*;
import com.nexahr.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final CustomRolePermissionRepository customRolePermissionRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public void seedDefaults(Company company) {
        if (!rolePermissionRepository.findByCompanyId(company.getId()).isEmpty()) {
            return;
        }
        for (Role role : Role.values()) {
            for (PermissionCode permission : PermissionCode.values()) {
                rolePermissionRepository.save(RolePermission.builder()
                        .company(company)
                        .role(role)
                        .permission(permission)
                        .granted(DefaultRolePermissions.isGrantedByDefault(role, permission))
                        .build());
            }
        }
    }

    @Override
    public UserPermissionsResponse getCurrentUserPermissions(Long userId, Role role, Long companyId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        CustomRole customRole = user != null ? user.getCustomRole() : null;

        if (customRole != null && customRole.isActive()) {
            List<PermissionCode> granted = customRolePermissionRepository
                    .findByCustomRoleIdAndGrantedTrue(customRole.getId()).stream()
                    .map(CustomRolePermission::getPermission)
                    .sorted(Comparator.comparing(Enum::name))
                    .toList();
            return UserPermissionsResponse.builder().permissions(granted).build();
        }

        List<PermissionCode> granted = rolePermissionRepository
                .findByCompanyIdAndRoleAndGrantedTrue(companyId, role).stream()
                .map(RolePermission::getPermission)
                .sorted(Comparator.comparing(Enum::name))
                .toList();
        return UserPermissionsResponse.builder().permissions(granted).build();
    }

    @Override
    public PermissionMatrixResponse getMatrix(Long companyId) {
        List<RolePermission> all = rolePermissionRepository.findByCompanyId(companyId);
        if (all.isEmpty()) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            seedDefaults(company);
            all = rolePermissionRepository.findByCompanyId(companyId);
        }

        Map<Role, List<PermissionMatrixResponse.PermissionGrant>> matrix = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            List<PermissionMatrixResponse.PermissionGrant> grants = all.stream()
                    .filter(rp -> rp.getRole() == role)
                    .map(rp -> PermissionMatrixResponse.PermissionGrant.builder()
                            .permission(rp.getPermission())
                            .granted(rp.isGranted())
                            .build())
                    .sorted(Comparator.comparing(g -> g.getPermission().name()))
                    .toList();
            matrix.put(role, grants);
        }

        List<PermissionMatrixResponse.PermissionItem> permissions = Arrays.stream(PermissionCode.values())
                .map(code -> PermissionMatrixResponse.PermissionItem.builder()
                        .code(code)
                        .label(code.getLabel())
                        .build())
                .toList();

        return PermissionMatrixResponse.builder()
                .permissions(permissions)
                .matrix(matrix)
                .build();
    }

    @Override
    @Transactional
    public PermissionMatrixResponse updatePermissions(Long companyId, List<RolePermissionUpdateRequest> updates) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        for (RolePermissionUpdateRequest update : updates) {
            RolePermission rp = rolePermissionRepository
                    .findByCompanyIdAndRoleAndPermission(companyId, update.getRole(), update.getPermission())
                    .orElse(RolePermission.builder()
                            .company(company)
                            .role(update.getRole())
                            .permission(update.getPermission())
                            .build());
            rp.setGranted(update.getGranted());
            rolePermissionRepository.save(rp);
        }
        return getMatrix(companyId);
    }

    @Override
    public boolean hasPermission(Long userId, Role role, Long companyId, PermissionCode permission) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        if (user != null && user.getCustomRole() != null && user.getCustomRole().isActive()) {
            return customRolePermissionRepository.findByCustomRoleIdAndGrantedTrue(user.getCustomRole().getId())
                    .stream()
                    .anyMatch(p -> p.getPermission() == permission);
        }
        if (companyId == null) {
            return DefaultRolePermissions.isGrantedByDefault(role, permission);
        }
        return rolePermissionRepository.findByCompanyIdAndRoleAndPermission(companyId, role, permission)
                .map(RolePermission::isGranted)
                .orElse(DefaultRolePermissions.isGrantedByDefault(role, permission));
    }
}
