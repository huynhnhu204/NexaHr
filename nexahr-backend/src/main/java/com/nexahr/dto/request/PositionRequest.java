package com.nexahr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionRequest {
    @NotBlank(message = "Position name is required")
    private String name;
    private BigDecimal baseSalary;
    private String description;
}
