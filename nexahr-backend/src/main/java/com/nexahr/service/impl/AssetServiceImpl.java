package com.nexahr.service.impl;

import com.nexahr.dto.request.AssetRequest;
import com.nexahr.dto.response.AssetAssignmentHistoryResponse;
import com.nexahr.dto.response.AssetResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.Asset;
import com.nexahr.entity.AssetAssignmentHistory;
import com.nexahr.entity.Employee;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.AssetStatus;
import com.nexahr.entity.enums.AssetType;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.AssetAssignmentHistoryRepository;
import com.nexahr.repository.AssetRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.service.AssetService;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetAssignmentHistoryRepository historyRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public PageResponse<AssetResponse> getAll(String search, AssetType type, AssetStatus status, Pageable pageable) {
        return PageUtil.toPageResponse(assetRepository.findWithFilters(search, type, status, pageable)
                .map(this::toResponse));
    }

    @Override
    public AssetResponse getById(Long id) {
        return toResponse(assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài sản")));
    }

    @Override
    @Transactional
    public AssetResponse create(AssetRequest request) {
        String code = request.getAssetCode() != null ? request.getAssetCode() :
                "AST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (assetRepository.existsByAssetCode(code)) {
            throw new BadRequestException("Mã tài sản đã tồn tại");
        }

        Asset asset = Asset.builder()
                .name(request.getName())
                .assetCode(code)
                .assetType(request.getAssetType())
                .description(request.getDescription())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .status(AssetStatus.AVAILABLE)
                .build();

        return toResponse(assetRepository.save(asset));
    }

    @Override
    @Transactional
    public AssetResponse update(Long id, AssetRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài sản"));

        asset.setName(request.getName());
        asset.setAssetType(request.getAssetType());
        asset.setDescription(request.getDescription());
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setPurchasePrice(request.getPurchasePrice());

        return toResponse(assetRepository.save(asset));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài sản"));
        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException("Không thể xóa tài sản đang được cấp phát");
        }
        assetRepository.delete(asset);
    }

    @Override
    @Transactional
    public AssetResponse assign(Long assetId, Long employeeId, User assignedBy, String note) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài sản"));
        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException("Tài sản đang được cấp phát cho nhân viên khác");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        LocalDateTime now = LocalDateTime.now();
        asset.setAssignedTo(employee);
        asset.setAssignedAt(now);
        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);

        historyRepository.save(AssetAssignmentHistory.builder()
                .asset(asset)
                .employee(employee)
                .assignedAt(now)
                .note(note)
                .assignedBy(assignedBy)
                .build());

        return toResponse(asset);
    }

    @Override
    @Transactional
    public AssetResponse returnAsset(Long assetId, String note) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài sản"));
        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new BadRequestException("Tài sản không ở trạng thái đang cấp phát");
        }

        historyRepository.findFirstByAssetIdAndReturnedAtIsNullOrderByAssignedAtDesc(assetId)
                .ifPresent(history -> {
                    history.setReturnedAt(LocalDateTime.now());
                    if (note != null) history.setNote(note);
                    historyRepository.save(history);
                });

        asset.setAssignedTo(null);
        asset.setAssignedAt(null);
        asset.setStatus(AssetStatus.AVAILABLE);
        return toResponse(assetRepository.save(asset));
    }

    @Override
    public List<AssetAssignmentHistoryResponse> getHistory(Long assetId) {
        assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài sản"));
        return historyRepository.findByAssetIdOrderByAssignedAtDesc(assetId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private AssetResponse toResponse(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .name(asset.getName())
                .assetCode(asset.getAssetCode())
                .assetType(asset.getAssetType())
                .description(asset.getDescription())
                .purchaseDate(asset.getPurchaseDate())
                .purchasePrice(asset.getPurchasePrice())
                .status(asset.getStatus())
                .assignedToId(asset.getAssignedTo() != null ? asset.getAssignedTo().getId() : null)
                .assignedToName(asset.getAssignedTo() != null ? asset.getAssignedTo().getFullName() : null)
                .assignedAt(asset.getAssignedAt())
                .createdAt(asset.getCreatedAt())
                .build();
    }

    private AssetAssignmentHistoryResponse toHistoryResponse(AssetAssignmentHistory history) {
        return AssetAssignmentHistoryResponse.builder()
                .id(history.getId())
                .assetId(history.getAsset().getId())
                .assetName(history.getAsset().getName())
                .employeeId(history.getEmployee().getId())
                .employeeName(history.getEmployee().getFullName())
                .assignedAt(history.getAssignedAt())
                .returnedAt(history.getReturnedAt())
                .note(history.getNote())
                .assignedByName(history.getAssignedBy() != null ? history.getAssignedBy().getUsername() : null)
                .build();
    }
}
