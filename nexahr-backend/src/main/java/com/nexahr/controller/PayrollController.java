package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.PayrollResponse;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.PayrollExportService;
import com.nexahr.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final PayrollExportService payrollExportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PageResponse<PayrollResponse>> getAll(
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(payrollService.getAll(month,
                PageRequest.of(page, size, Sort.by("salaryMonth").descending())));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<PayrollResponse>> getMy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long employeeId = userDetails.getUser().getEmployee().getId();
        return ApiResponse.success(payrollService.getMy(employeeId,
                PageRequest.of(page, size, Sort.by("salaryMonth").descending())));
    }

    @GetMapping("/{id}")
    public ApiResponse<PayrollResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(payrollService.getByIdForUser(id, userDetails.getUser()));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<List<PayrollResponse>> generate(@RequestBody(required = false) Map<String, String> body) {
        String month = body != null ? body.get("month") : null;
        return ApiResponse.success("Tạo bảng lương thành công", payrollService.generate(month));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PayrollResponse> approve(@PathVariable Long id) {
        return ApiResponse.success("Phê duyệt bảng lương thành công", payrollService.approve(id));
    }

    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PayrollResponse> markPaid(@PathVariable Long id) {
        return ApiResponse.success("Đánh dấu đã thanh toán thành công", payrollService.markPaid(id));
    }

    @GetMapping("/{id}/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long id) {
        byte[] data = payrollExportService.exportToExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll-" + id + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/{id}/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        byte[] data = payrollExportService.exportToPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
