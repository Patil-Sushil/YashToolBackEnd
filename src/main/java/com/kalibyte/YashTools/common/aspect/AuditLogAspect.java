package com.kalibyte.YashTools.common.aspect;

import com.kalibyte.YashTools.auth.entity.AuditLog;
import com.kalibyte.YashTools.auth.repository.AuditLogRepository;
import com.kalibyte.YashTools.auth.security.token.CustomUserDetails;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning(pointcut = "@annotation(loggableAction)", returning = "result")
    public void logAction(JoinPoint joinPoint, LoggableAction loggableAction, Object result) {
        try {
            String action = loggableAction.value();
            if (action.isEmpty()) {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                action = signature.getMethod().getName();
            }

            UUID userId = getCurrentUserId();

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: action={}, user={}", action, userId);

        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null; // System action or unauthenticated
    }
}
