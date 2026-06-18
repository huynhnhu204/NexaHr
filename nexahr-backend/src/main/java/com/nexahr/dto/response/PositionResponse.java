package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PositionResponse {
    private Long id;
    private String name;
    private BigDecimal baseSalary;
    private String description;
}
