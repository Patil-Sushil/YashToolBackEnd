package com.kalibyte.YashTools.audit.mapper;


import com.kalibyte.YashTools.audit.dto.response.AuditLogResponse;
import com.kalibyte.YashTools.audit.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);

    List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);
}