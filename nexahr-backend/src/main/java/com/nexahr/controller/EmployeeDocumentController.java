package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.EmployeeDocumentResponse;
import com.nexahr.entity.enums.DocumentType;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.EmployeeDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}/documents")
@RequiredArgsConstructor
public class EmployeeDocumentController {

    private final EmployeeDocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<EmployeeDocumentResponse> upload(
            @PathVariable Long employeeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam DocumentType documentType,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("Tải tài liệu thành công",
                documentService.upload(employeeId, file, documentType, userDetails.getUser()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<List<EmployeeDocumentResponse>> getAll(@PathVariable Long employeeId) {
        return ApiResponse.success(documentService.getByEmployeeId(employeeId));
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> delete(@PathVariable Long employeeId, @PathVariable Long documentId) {
        documentService.delete(employeeId, documentId);
        return ApiResponse.success("Xóa tài liệu thành công", null);
    }
}
