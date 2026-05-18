package com.kalibyte.YashTools.labors.report.mapper;

import com.kalibyte.YashTools.labors.attendance.entity.Attendance;
import com.kalibyte.YashTools.labors.report.dto.LaborAttendanceReportDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportMapper {

    @Mapping(target = "pieceCompleted", source = "piecesCompleted")
    LaborAttendanceReportDTO toAttendanceReport(Attendance attendance);
}
