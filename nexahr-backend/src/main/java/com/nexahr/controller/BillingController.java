package com.nexahr.controller;

import com.nexahr.dto.request.CheckoutRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.BillingInvoiceResponse;
import com.nexahr.dto.response.CheckoutResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.BillingService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.success(billingService.createCheckoutSession(requireCompanyId(), request));
    }

    @PostMapping("/checkout/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CheckoutResponse> confirmMockCheckout(@RequestParam String sessionId) {
        return ApiResponse.success("Thanh toán thành công",
                billingService.confirmMockCheckout(requireCompanyId(), sessionId));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<BillingInvoiceResponse>> getInvoices(
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(billingService.getBillingHistory(requireCompanyId(), pageable));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
