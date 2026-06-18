package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DataImportResultResponse {
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<String> errors;
}
