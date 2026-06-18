package com.nexahr.service.impl;

import com.nexahr.dto.request.EmployeeRequest;
import com.nexahr.dto.response.EmployeeResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.*;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.entity.enums.Role;
import com.nexahr.entity.enums.UserStatus;
import com.nexahr.entity.enums.WebhookEvent;
import com.nexahr.service.WebhookService;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.mapper.EmployeeMapper;
import com.nexahr.repository.*;
import com.nexahr.tenant.TenantContext;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper mapper;
    private final WebhookService webhookService;

    public PageResponse<EmployeeResponse> getAll(String search, Long departmentId, EmploymentStatus status, Pageable pageable) {
        Page<Employee> page = employeeRepository.findWithFilters(requireCompanyId(), search, departmentId, status, pageable);
        return PageUtil.toPageResponse(page.map(mapper::toResponse));
    }

    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));
        return mapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        Long companyId = requireCompanyId();
        String email = request.getEmail() != null ? request.getEmail() : request.getFullName().toLowerCase().replace(" ", ".") + "@nexahr.com";
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã tồn tại");
        }

        String username = request.getUsername() != null ? request.getUsername() : email.split("@")[0];
        String password = request.getPassword() != null ? request.getPassword() : "123456";

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        Department department = request.getDepartmentId() != null ?
                departmentRepository.findById(request.getDepartmentId()).orElse(null) : null;
        if (department != null && !department.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Phòng ban không thuộc công ty hiện tại");
        }
        Position position = request.getPositionId() != null ?
                positionRepository.findById(request.getPositionId()).orElse(null) : null;
        if (position != null && !position.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Chức vụ không thuộc công ty hiện tại");
        }

        String code = request.getEmployeeCode() != null ? request.getEmployeeCode() :
                "EMP" + String.format("%04d", user.getId());

        Employee employee = Employee.builder()
                .user(user)
                .company(company)
                .employeeCode(code)
                .fullName(request.getFullName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .phone(request.getPhone())
                .address(request.getAddress())
                .department(department)
                .position(position)
                .hireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now())
                .employmentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : EmploymentStatus.ACTIVE)
                .annualLeaveBalance(12)
                .nationalId(request.getNationalId())
                .personalEmail(request.getPersonalEmail())
                .contractType(request.getContractType())
                .contractStartDate(request.getContractStartDate())
                .contractEndDate(request.getContractEndDate())
                .manager(request.getManagerId() != null ?
                        employeeRepository.findByIdAndCompanyId(request.getManagerId(), companyId).orElse(null) : null)
                .build();
        employeeRepository.save(employee);
        user.setEmployee(employee);

        webhookService.dispatch(companyId, WebhookEvent.EMPLOYEE_CREATED, java.util.Map.of(
                "employeeId", employee.getId(),
                "employeeCode", employee.getEmployeeCode(),
                "fullName", employee.getFullName(),
                "email", email
        ));

        return mapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Long companyId = requireCompanyId();
        Employee employee = employeeRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        employee.setFullName(request.getFullName());
        employee.setGender(request.getGender());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId()).orElse(null);
            if (dept != null && !dept.getCompany().getId().equals(companyId)) {
                throw new BadRequestException("Phòng ban không thuộc công ty hiện tại");
            }
            employee.setDepartment(dept);
        }
        if (request.getPositionId() != null) {
            Position pos = positionRepository.findById(request.getPositionId()).orElse(null);
            if (pos != null && !pos.getCompany().getId().equals(companyId)) {
                throw new BadRequestException("Chức vụ không thuộc công ty hiện tại");
            }
            employee.setPosition(pos);
        }
        if (request.getHireDate() != null) employee.setHireDate(request.getHireDate());
        if (request.getEmploymentStatus() != null) employee.setEmploymentStatus(request.getEmploymentStatus());
        if (request.getNationalId() != null) employee.setNationalId(request.getNationalId());
        if (request.getPersonalEmail() != null) employee.setPersonalEmail(request.getPersonalEmail());
        if (request.getContractType() != null) employee.setContractType(request.getContractType());
        if (request.getContractStartDate() != null) employee.setContractStartDate(request.getContractStartDate());
        if (request.getContractEndDate() != null) employee.setContractEndDate(request.getContractEndDate());
        if (request.getManagerId() != null) {
            employee.setManager(employeeRepository.findByIdAndCompanyId(request.getManagerId(), companyId).orElse(null));
        }

        return mapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = employeeRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));
        employee.setEmploymentStatus(EmploymentStatus.RESIGNED);
        if (employee.getUser() != null) {
            employee.getUser().setStatus(UserStatus.INACTIVE);
        }
        employeeRepository.save(employee);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
