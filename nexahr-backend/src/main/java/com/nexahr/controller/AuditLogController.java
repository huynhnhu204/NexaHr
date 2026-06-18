package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.AuditLogResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.security.Audited;
import com.nexahr.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("@permissionChecker.has('AUDIT_VIEW')")
    public ApiResponse<PageResponse<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType) {
        return ApiResponse.success(auditLogService.getLogs(page, size, search, action, entityType));
    }

    @GetMapping("/export")
    @PreAuthorize("@permissionChecker.has('AUDIT_VIEW')")
    @Audited(action = "EXPORT", entityType = "AUDIT", details = "Xuất nhật ký bảo mật CSV")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType) {
        byte[] csv = auditLogService.exportCsv(search, action, entityType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
