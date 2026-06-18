package com.nexahr.controller;

import com.nexahr.dto.request.AssetRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.AssetAssignmentHistoryResponse;
import com.nexahr.dto.response.AssetResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.enums.AssetStatus;
import com.nexahr.entity.enums.AssetType;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PageResponse<AssetResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(assetService.getAll(search, type, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<AssetResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(assetService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<AssetResponse> create(@Valid @RequestBody AssetRequest request) {
        return ApiResponse.success("Tạo tài sản thành công", assetService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<AssetResponse> update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        return ApiResponse.success("Cập nhật tài sản thành công", assetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ApiResponse.success("Xóa tài sản thành công", null);
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<AssetResponse> assign(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long employeeId = Long.valueOf(body.get("employeeId").toString());
        String note = body.get("note") != null ? body.get("note").toString() : null;
        return ApiResponse.success("Cấp phát tài sản thành công",
                assetService.assign(id, employeeId, userDetails.getUser(), note));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<AssetResponse> returnAsset(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        return ApiResponse.success("Thu hồi tài sản thành công", assetService.returnAsset(id, note));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<List<AssetAssignmentHistoryResponse>> getHistory(@PathVariable Long id) {
        return ApiResponse.success(assetService.getHistory(id));
    }
}
