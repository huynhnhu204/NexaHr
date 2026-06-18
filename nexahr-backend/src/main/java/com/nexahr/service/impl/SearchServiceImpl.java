package com.nexahr.service.impl;

import com.nexahr.dto.response.SearchResultResponse;
import com.nexahr.entity.Department;
import com.nexahr.entity.Employee;
import com.nexahr.entity.Position;
import com.nexahr.exception.BadRequestException;
import com.nexahr.repository.DepartmentRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.repository.PositionRepository;
import com.nexahr.service.SearchService;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    @Override
    public List<SearchResultResponse> globalSearch(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        Long companyId = requireCompanyId();
        String q = query.trim();
        List<SearchResultResponse> results = new ArrayList<>();

        employeeRepository.findWithFilters(companyId, q, null, null, PageRequest.of(0, 5))
                .getContent()
                .forEach(e -> results.add(toEmployeeResult(e)));

        departmentRepository.findByCompanyId(companyId).stream()
                .filter(d -> d.getName().toLowerCase().contains(q.toLowerCase()))
                .limit(5)
                .forEach(d -> results.add(toDepartmentResult(d)));

        positionRepository.findByCompanyId(companyId).stream()
                .filter(p -> p.getName().toLowerCase().contains(q.toLowerCase()))
                .limit(5)
                .forEach(p -> results.add(toPositionResult(p)));

        return results;
    }

    private SearchResultResponse toEmployeeResult(Employee e) {
        return SearchResultResponse.builder()
                .type("employee")
                .id(e.getId())
                .title(e.getFullName())
                .subtitle(e.getEmployeeCode() + (e.getDepartment() != null ? " · " + e.getDepartment().getName() : ""))
                .build();
    }

    private SearchResultResponse toDepartmentResult(Department d) {
        return SearchResultResponse.builder()
                .type("department")
                .id(d.getId())
                .title(d.getName())
                .subtitle(d.getDescription() != null ? d.getDescription() : "Phòng ban")
                .build();
    }

    private SearchResultResponse toPositionResult(Position p) {
        return SearchResultResponse.builder()
                .type("position")
                .id(p.getId())
                .title(p.getName())
                .subtitle(p.getDescription() != null ? p.getDescription() : "Chức vụ")
                .build();
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
