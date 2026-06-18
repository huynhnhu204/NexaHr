package com.nexahr.dto.request;

import com.nexahr.entity.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetRequest {
    @NotBlank(message = "Asset name is required")
    private String name;

    private String assetCode;

    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    private String description;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
}
