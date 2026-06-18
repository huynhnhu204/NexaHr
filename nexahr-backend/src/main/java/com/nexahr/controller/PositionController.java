package com.nexahr.controller;

import com.nexahr.dto.request.PositionRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.PositionResponse;
import com.nexahr.service.impl.PositionServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionServiceImpl positionService;

    @GetMapping
    public ApiResponse<List<PositionResponse>> getAll() {
        return ApiResponse.success(positionService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PositionResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(positionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PositionResponse> create(@Valid @RequestBody PositionRequest request) {
        return ApiResponse.success("Position created", positionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PositionResponse> update(@PathVariable Long id, @Valid @RequestBody PositionRequest request) {
        return ApiResponse.success("Position updated", positionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ApiResponse.success("Position deleted", null);
    }
}
