package com.nexahr.controller;

import com.nexahr.dto.request.WorkflowRuleRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.WorkflowRuleResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.WorkflowService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public ApiResponse<List<WorkflowRuleResponse>> list() {
        return ApiResponse.success(workflowService.list(requireCompanyId()));
    }

    @PostMapping
    public ApiResponse<WorkflowRuleResponse> create(@Valid @RequestBody WorkflowRuleRequest request) {
        return ApiResponse.success(workflowService.create(requireCompanyId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkflowRuleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowRuleRequest request) {
        return ApiResponse.success(workflowService.update(requireCompanyId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workflowService.delete(requireCompanyId(), id);
        return ApiResponse.success("Đã xóa quy trình", null);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
