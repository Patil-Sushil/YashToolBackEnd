package com.kalibyte.YashTools.common.aspect;
 
import com.kalibyte.YashTools.audit.entity.AuditLog;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditStatus;
import com.kalibyte.YashTools.audit.service.AuditLogService;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(loggableAction)")
    public Object logAction(ProceedingJoinPoint joinPoint, LoggableAction loggableAction) throws Throwable {
        long startTime = System.currentTimeMillis();
        AuditStatus status = AuditStatus.SUCCESS;
        String errorMessage = null;
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            status = AuditStatus.FAILURE;
            errorMessage = t.getMessage();
            throw t;
        } finally {
            try {
                long durationMs = System.currentTimeMillis() - startTime;

                UUID userId = SecurityUtils.getCurrentUserId();
                String username = SecurityUtils.getCurrentUsername();

                AuditAction action = loggableAction.action();
                String actionDescription = loggableAction.value();
                if (actionDescription.isEmpty()) {
                    actionDescription = action.getDisplayName();
                }

                String entityType = loggableAction.entityType();

                // Retrieve IP address and User Agent
                String ipAddress = null;
                String userAgent = null;
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ipAddress = getClientIp(request);
                    userAgent = request.getHeader("User-Agent");
                }

                AuditLog auditLog = AuditLog.builder()
                        .userId(userId)
                        .username(username)
                        .action(action)
                        .actionDescription(actionDescription)
                        .entityType(entityType)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .status(status)
                        .errorMessage(errorMessage)
                        .durationMs(durationMs)
                        .timestamp(LocalDateTime.now())
                        .build();

                auditLogService.log(auditLog);
            } catch (Exception e) {
                log.error("Failed to save audit log in aspect", e);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
