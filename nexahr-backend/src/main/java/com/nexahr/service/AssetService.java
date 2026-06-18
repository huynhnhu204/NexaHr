package com.nexahr.service;

import com.nexahr.dto.request.AssetRequest;
import com.nexahr.dto.response.AssetAssignmentHistoryResponse;
import com.nexahr.dto.response.AssetResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.AssetStatus;
import com.nexahr.entity.enums.AssetType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssetService {
    PageResponse<AssetResponse> getAll(String search, AssetType type, AssetStatus status, Pageable pageable);
    AssetResponse getById(Long id);
    AssetResponse create(AssetRequest request);
    AssetResponse update(Long id, AssetRequest request);
    void delete(Long id);
    AssetResponse assign(Long assetId, Long employeeId, User assignedBy, String note);
    AssetResponse returnAsset(Long assetId, String note);
    List<AssetAssignmentHistoryResponse> getHistory(Long assetId);
}
