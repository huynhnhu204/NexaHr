package com.nexahr.dto.response;

import com.nexahr.entity.enums.DocumentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeDocumentResponse {
    private Long id;
    private Long employeeId;
    private String fileName;
    private String originalName;
    private String filePath;
    private String downloadUrl;
    private Long fileSize;
    private DocumentType documentType;
    private String uploadedByName;
    private LocalDateTime createdAt;
}
