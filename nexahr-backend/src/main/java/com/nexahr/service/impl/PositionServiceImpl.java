package com.nexahr.service.impl;

import com.nexahr.dto.request.PositionRequest;
import com.nexahr.dto.response.PositionResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.Position;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.PositionRepository;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl {

    private final PositionRepository positionRepository;
    private final CompanyRepository companyRepository;

    public List<PositionResponse> getAll() {
        return positionRepository.findByCompanyId(requireCompanyId()).stream()
                .map(this::toResponse).toList();
    }

    public PositionResponse getById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức vụ"));
        if (!position.getCompany().getId().equals(requireCompanyId())) {
            throw new ResourceNotFoundException("Không tìm thấy chức vụ");
        }
        return toResponse(position);
    }

    @Transactional
    public PositionResponse create(PositionRequest request) {
        Long companyId = requireCompanyId();
        if (positionRepository.existsByNameAndCompanyId(request.getName(), companyId)) {
            throw new BadRequestException("Tên chức vụ đã tồn tại");
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        Position position = Position.builder()
                .name(request.getName())
                .baseSalary(request.getBaseSalary())
                .description(request.getDescription())
                .company(company)
                .build();
        return toResponse(positionRepository.save(position));
    }

    @Transactional
    public PositionResponse update(Long id, PositionRequest request) {
        Long companyId = requireCompanyId();
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức vụ"));
        if (!position.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy chức vụ");
        }
        position.setName(request.getName());
        position.setBaseSalary(request.getBaseSalary());
        position.setDescription(request.getDescription());
        return toResponse(positionRepository.save(position));
    }

    @Transactional
    public void delete(Long id) {
        Long companyId = requireCompanyId();
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức vụ"));
        if (!position.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy chức vụ");
        }
        positionRepository.delete(position);
    }

    private PositionResponse toResponse(Position p) {
        return PositionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .baseSalary(p.getBaseSalary())
                .description(p.getDescription())
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
