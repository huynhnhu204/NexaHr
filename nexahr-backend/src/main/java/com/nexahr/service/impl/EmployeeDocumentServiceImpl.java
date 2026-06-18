package com.nexahr.service.impl;

import com.nexahr.dto.response.EmployeeDocumentResponse;
import com.nexahr.dto.response.EmployeeTimelineEventResponse;
import com.nexahr.entity.*;
import com.nexahr.entity.enums.DocumentType;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.*;
import com.nexahr.service.EmployeeDocumentService;
import com.nexahr.service.FileStorageService;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeDocumentServiceImpl implements EmployeeDocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollRepository payrollRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public EmployeeDocumentResponse upload(Long employeeId, MultipartFile file, DocumentType documentType, User uploadedBy) {
        Employee employee = requireEmployeeInTenant(employeeId);
        if (documentType == null) {
            throw new BadRequestException("Loại tài liệu không được để trống");
        }

        String subDir = "employees/" + employeeId;
        String storedPath = fileStorageService.store(file, subDir);

        EmployeeDocument document = EmployeeDocument.builder()
                .employee(employee)
                .fileName(storedPath.substring(storedPath.lastIndexOf('/') + 1))
                .originalName(file.getOriginalFilename())
                .filePath(storedPath)
                .fileSize(file.getSize())
                .documentType(documentType)
                .uploadedBy(uploadedBy)
                .build();

        if (documentType == DocumentType.AVATAR) {
            employee.setAvatar(storedPath);
            employeeRepository.save(employee);
        }

        return toResponse(documentRepository.save(document));
    }

    @Override
    public List<EmployeeDocumentResponse> getByEmployeeId(Long employeeId) {
        requireEmployeeInTenant(employeeId);
        return documentRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long employeeId, Long documentId) {
        EmployeeDocument document = documentRepository.findByIdAndEmployeeId(documentId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));
        fileStorageService.delete(document.getFilePath());
        documentRepository.delete(document);
    }

    @Override
    public List<EmployeeTimelineEventResponse> getTimeline(Long employeeId) {
        Employee employee = requireEmployeeInTenant(employeeId);

        List<EmployeeTimelineEventResponse> events = new ArrayList<>();

        if (employee.getHireDate() != null) {
            events.add(EmployeeTimelineEventResponse.builder()
                    .type("HIRE")
                    .title("Ngày vào làm")
                    .description("Nhân viên gia nhập công ty")
                    .occurredAt(employee.getHireDate().atStartOfDay())
                    .build());
        }

        if (employee.getContractStartDate() != null) {
            events.add(EmployeeTimelineEventResponse.builder()
                    .type("CONTRACT")
                    .title("Bắt đầu hợp đồng")
                    .description("Loại HĐ: " + (employee.getContractType() != null ? employee.getContractType() : "N/A"))
                    .occurredAt(employee.getContractStartDate().atStartOfDay())
                    .build());
        }

        documentRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).forEach(doc ->
                events.add(EmployeeTimelineEventResponse.builder()
                        .type("DOCUMENT")
                        .title("Tải lên: " + doc.getDocumentType().name())
                        .description(doc.getOriginalName())
                        .occurredAt(doc.getCreatedAt())
                        .build()));

        leaveRepository.findByEmployeeId(employeeId, org.springframework.data.domain.Pageable.unpaged())
                .getContent().forEach(leave ->
                        events.add(EmployeeTimelineEventResponse.builder()
                                .type("LEAVE")
                                .title("Đơn nghỉ phép: " + leave.getLeaveType().name())
                                .description(leave.getStatus().name() + " - " + leave.getStartDate() + " đến " + leave.getEndDate())
                                .occurredAt(leave.getCreatedAt())
                                .build()));

        payrollRepository.findByEmployeeId(employeeId, org.springframework.data.domain.Pageable.unpaged())
                .getContent().forEach(payroll ->
                        events.add(EmployeeTimelineEventResponse.builder()
                                .type("PAYROLL")
                                .title("Bảng lương tháng " + payroll.getSalaryMonth())
                                .description("Trạng thái: " + payroll.getStatus().name())
                                .occurredAt(LocalDate.parse(payroll.getSalaryMonth() + "-01").atStartOfDay())
                                .build()));

        events.sort(Comparator.comparing(EmployeeTimelineEventResponse::getOccurredAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return events;
    }

    private Employee requireEmployeeInTenant(Long employeeId) {
        return employeeRepository.findByIdAndCompanyId(employeeId, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }

    private EmployeeDocumentResponse toResponse(EmployeeDocument doc) {
        return EmployeeDocumentResponse.builder()
                .id(doc.getId())
                .employeeId(doc.getEmployee().getId())
                .fileName(doc.getFileName())
                .originalName(doc.getOriginalName())
                .filePath(doc.getFilePath())
                .downloadUrl("/api/files/" + doc.getFilePath())
                .fileSize(doc.getFileSize())
                .documentType(doc.getDocumentType())
                .uploadedByName(doc.getUploadedBy() != null ? doc.getUploadedBy().getUsername() : null)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
