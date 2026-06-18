package com.nexahr.controller;

import com.nexahr.dto.request.AnnouncementRequest;
import com.nexahr.dto.response.AnnouncementResponse;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.AnnouncementService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ApiResponse<Page<AnnouncementResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(announcementService.list(requireCompanyId(), pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<AnnouncementResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AnnouncementRequest request) {
        return ApiResponse.success("Đăng thông báo thành công",
                announcementService.create(requireCompanyId(), userDetails.getUser().getId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(requireCompanyId(), id);
        return ApiResponse.success("Đã xóa thông báo", null);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
