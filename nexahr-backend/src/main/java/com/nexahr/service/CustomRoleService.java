package com.nexahr.service;

import com.nexahr.dto.request.CustomRoleRequest;
import com.nexahr.dto.response.CustomRoleResponse;
import com.nexahr.entity.Company;

import java.util.List;

public interface CustomRoleService {
    List<CustomRoleResponse> list(Long companyId);
    CustomRoleResponse create(Long companyId, CustomRoleRequest request);
    CustomRoleResponse update(Long companyId, Long id, CustomRoleRequest request);
    void delete(Long companyId, Long id);
    void assignToUser(Long companyId, Long userId, Long customRoleId);
    void seedDemoRoles(Company company);
}
