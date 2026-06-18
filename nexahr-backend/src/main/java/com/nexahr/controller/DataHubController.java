package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.DataImportResultResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.security.Audited;
import com.nexahr.service.DataHubService;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/data-hub")
@RequiredArgsConstructor
@PreAuthorize("@permissionChecker.has('EMPLOYEE_MANAGE')")
public class DataHubController {

    private final DataHubService dataHubService;

    @GetMapping("/export/employees")
    @Audited(action = "EXPORT", entityType = "EMPLOYEE", details = "Xuất danh sách nhân viên CSV")
    public ResponseEntity<byte[]> exportEmployees() {
        byte[] csv = dataHubService.exportEmployeesCsv(requireCompanyId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> importTemplate() {
        byte[] csv = dataHubService.employeeImportTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee-import-template.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @PostMapping(value = "/import/employees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Audited(action = "IMPORT", entityType = "EMPLOYEE", details = "Import nhân viên từ CSV")
    public ApiResponse<DataImportResultResponse> importEmployees(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(dataHubService.importEmployees(requireCompanyId(), file));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
