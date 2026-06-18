package com.nexahr.service.impl;

import com.nexahr.dto.response.AuditLogResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.AuditLog;
import com.nexahr.entity.User;
import com.nexahr.repository.AuditLogRepository;
import com.nexahr.service.AuditLogService;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(User user, String action, String entityType, Long entityId, String details,
                    String ipAddress, String browser, String device) {
        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .browser(browser)
                .device(device)
                .build());
    }

    @Override
    public PageResponse<AuditLogResponse> getLogs(int page, int size, String search, String action, String entityType) {
        return PageUtil.toPageResponse(auditLogRepository.findWithFilters(
                search, action, entityType,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(this::toResponse));
    }

    @Override
    public byte[] exportCsv(String search, String action, String entityType) {
        var logs = auditLogRepository.findWithFilters(
                search, action, entityType,
                PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        StringBuilder csv = new StringBuilder("id,time,user,action,entityType,entityId,ip,details\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AuditLog log : logs) {
            csv.append(log.getId()).append(',')
                    .append(log.getCreatedAt() != null ? log.getCreatedAt().format(fmt) : "").append(',')
                    .append(escape(log.getUser() != null ? log.getUser().getUsername() : "system")).append(',')
                    .append(escape(log.getAction())).append(',')
                    .append(escape(log.getEntityType())).append(',')
                    .append(log.getEntityId() != null ? log.getEntityId() : "").append(',')
                    .append(escape(log.getIpAddress())).append(',')
                    .append(escape(log.getDetails())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .username(log.getUser() != null ? log.getUser().getUsername() : "system")
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .ipAddress(log.getIpAddress())
                .browser(log.getBrowser())
                .device(log.getDevice())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
