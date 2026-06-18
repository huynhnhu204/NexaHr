package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResultResponse {
    private String type;
    private Long id;
    private String title;
    private String subtitle;
}
