package com.kalibyte.YashTools.quotation.aspect;

import com.kalibyte.YashTools.audit.entity.AuditLog;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditStatus;
import com.kalibyte.YashTools.audit.service.AuditLogService;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cross-cutting audit capture for every public QuotationService method.
 * <p>Logs: actor, action, entity id, status transition.</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class QuotationAuditAspect {

    private final AuditLogService auditLogService;
    private final QuotationSecurityService security;

    @Pointcut("execution(* com.kalibyte.YashTools.quotation.service.QuotationService.*(..))")
    public void quotationServiceOps() {}

    @AfterReturning(
            pointcut = "quotationServiceOps() && execution(* create*(..))",
            returning = "result")
    public void afterCreate(JoinPoint joinPoint, Object result) {
        try {
            if (result == null) return;
            String method = joinPoint.getSignature().getName();
            Object quotationNo = result.getClass().getMethod("getQuotationNo").invoke(result);
            Object quotationId = result.getClass().getMethod("getQuotationId").invoke(result);

            AuditLog auditLog = AuditLog.builder()
                    .username(security.currentUsername())
                    .action(mapToAction(method))
                    .actionDescription("Created " + quotationNo)
                    .entityType("Quotation")
                    .entityId(quotationId == null ? null : UUID.fromString(quotationId.toString()))
                    .status(AuditStatus.SUCCESS)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogService.log(auditLog);
        } catch (Exception e) {
            log.warn("Audit capture failed on create: {}", e.getMessage());
        }
    }

    @AfterReturning(
            pointcut = "quotationServiceOps() && (execution(* updateDraft(..))" +
                    " || execution(* revise(..)) || execution(* lockFinal(..))" +
                    " || execution(* cancel(..)))",
            returning = "result")
    public void afterMutation(JoinPoint joinPoint, Object result) {
        try {
            if (result == null) return;
            String method = joinPoint.getSignature().getName();
            Object quotationId = result.getClass().getMethod("getQuotationId").invoke(result);

            AuditLog auditLog = AuditLog.builder()
                    .username(security.currentUsername())
                    .action(mapToAction(method))
                    .actionDescription("Action " + method + " by " + security.currentUsername())
                    .entityType("Quotation")
                    .entityId(quotationId == null ? null : UUID.fromString(quotationId.toString()))
                    .status(AuditStatus.SUCCESS)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogService.log(auditLog);
        } catch (Exception e) {
            log.warn("Audit capture failed on mutation: {}", e.getMessage());
        }
    }

    private AuditAction mapToAction(String methodName) {
        if (methodName.startsWith("create")) {
            return AuditAction.QUOTATION_CREATED;
        }
        return switch (methodName) {
            case "updateDraft" -> AuditAction.QUOTATION_UPDATED;
            case "revise" -> AuditAction.QUOTATION_REVISED;
            case "lockFinal" -> AuditAction.QUOTATION_LOCKED;
            case "cancel" -> AuditAction.QUOTATION_CANCELLED;
            default -> AuditAction.OTHER;
        };
    }
}