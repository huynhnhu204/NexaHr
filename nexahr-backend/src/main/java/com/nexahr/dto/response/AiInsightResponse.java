package com.nexahr.dto.response;

import com.nexahr.entity.enums.InsightSeverity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiInsightResponse {
    private String id;
    private String title;
    private String description;
    private InsightSeverity severity;
    private String category;
    private String actionLabel;
    private String actionPath;
}
