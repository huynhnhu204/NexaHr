package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssetAssignmentHistoryResponse {
    private Long id;
    private Long assetId;
    private String assetName;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
    private String note;
    private String assignedByName;
}
