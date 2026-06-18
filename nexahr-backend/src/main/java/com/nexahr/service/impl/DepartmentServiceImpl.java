package com.nexahr.service.impl;

import com.nexahr.dto.request.DepartmentRequest;
import com.nexahr.dto.response.DepartmentResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.Department;
import com.nexahr.entity.Employee;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.mapper.EmployeeMapper;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.DepartmentRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeMapper mapper;

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findByCompanyId(requireCompanyId()).stream()
                .map(mapper::toDepartmentResponse).toList();
    }

    public DepartmentResponse getById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban"));
        if (!dept.getCompany().getId().equals(requireCompanyId())) {
            throw new ResourceNotFoundException("Không tìm thấy phòng ban");
        }
        return mapper.toDepartmentResponse(dept);
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        Long companyId = requireCompanyId();
        if (departmentRepository.existsByNameAndCompanyId(request.getName(), companyId)) {
            throw new BadRequestException("Tên phòng ban đã tồn tại");
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        Employee manager = request.getManagerId() != null ?
                employeeRepository.findByIdAndCompanyId(request.getManagerId(), companyId).orElse(null) : null;

        Department dept = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .manager(manager)
                .company(company)
                .build();
        return mapper.toDepartmentResponse(departmentRepository.save(dept));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Long companyId = requireCompanyId();
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban"));
        if (!dept.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy phòng ban");
        }
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        if (request.getManagerId() != null) {
            dept.setManager(employeeRepository.findByIdAndCompanyId(request.getManagerId(), companyId).orElse(null));
        }
        return mapper.toDepartmentResponse(departmentRepository.save(dept));
    }

    @Transactional
    public void delete(Long id) {
        Long companyId = requireCompanyId();
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban"));
        if (!dept.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy phòng ban");
        }
        if (!dept.getEmployees().isEmpty()) {
            throw new BadRequestException("Không thể xóa phòng ban còn nhân viên");
        }
        departmentRepository.delete(dept);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
