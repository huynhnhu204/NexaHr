package com.nexahr.service;

import com.nexahr.dto.response.DataImportResultResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DataHubService {
    byte[] exportEmployeesCsv(Long companyId);
    byte[] employeeImportTemplate();
    DataImportResultResponse importEmployees(Long companyId, MultipartFile file);
}
