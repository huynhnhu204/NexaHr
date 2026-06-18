package com.nexahr.service;

import com.nexahr.dto.response.EmployeeDocumentResponse;
import com.nexahr.dto.response.EmployeeTimelineEventResponse;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeDocumentService {
    EmployeeDocumentResponse upload(Long employeeId, MultipartFile file, DocumentType documentType, User uploadedBy);
    List<EmployeeDocumentResponse> getByEmployeeId(Long employeeId);
    void delete(Long employeeId, Long documentId);
    List<EmployeeTimelineEventResponse> getTimeline(Long employeeId);
}
