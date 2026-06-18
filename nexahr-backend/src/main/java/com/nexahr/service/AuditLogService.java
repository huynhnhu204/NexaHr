package com.nexahr.service;

import com.nexahr.dto.response.AuditLogResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.User;

public interface AuditLogService {
    void log(User user, String action, String entityType, Long entityId, String details, String ipAddress, String browser, String device);
    PageResponse<AuditLogResponse> getLogs(int page, int size, String search, String action, String entityType);
    byte[] exportCsv(String search, String action, String entityType);
}
