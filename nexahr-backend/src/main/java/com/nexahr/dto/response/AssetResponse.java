package com.nexahr.dto.response;

import com.nexahr.entity.enums.AssetStatus;
import com.nexahr.entity.enums.AssetType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AssetResponse {
    private Long id;
    private String name;
    private String assetCode;
    private AssetType assetType;
    private String description;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private AssetStatus status;
    private Long assignedToId;
    private String assignedToName;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
}
