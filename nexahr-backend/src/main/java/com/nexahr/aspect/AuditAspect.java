package com.nexahr.aspect;

import com.nexahr.entity.User;
import com.nexahr.security.Audited;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.AuditLogService;
import com.nexahr.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @AfterReturning("@annotation(audited)")
    public void logAudit(JoinPoint joinPoint, Audited audited) {
        User user = currentUser();
        HttpServletRequest request = RequestContextUtil.currentRequest();
        String details = audited.details();
        if (details.isBlank()) {
            details = joinPoint.getSignature().getName();
        }

        Long entityId = extractEntityId(joinPoint);

        auditLogService.log(
                user,
                audited.action(),
                audited.entityType(),
                entityId,
                details,
                RequestContextUtil.clientIp(request),
                RequestContextUtil.userAgent(request),
                null
        );
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser();
        }
        return null;
    }

    private Long extractEntityId(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] names = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < names.length; i++) {
            if ("id".equals(names[i]) && args[i] instanceof Long longId) {
                return longId;
            }
        }
        return null;
    }
}
