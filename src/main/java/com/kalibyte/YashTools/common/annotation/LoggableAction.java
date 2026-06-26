package com.kalibyte.YashTools.common.annotation;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableAction {

    // Human-readable description
    String value() default "";

    // The audit action type
    AuditAction action() default AuditAction.OTHER;

    // Entity type being operated on
    String entityType() default "";
}
