package com.kalibyte.YashTools.workorder.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderResponse {
    private UUID id;
    private String workOrderNo;
    private UUID quotationId;
    private String quotationNo;
    private String status;
    private UUID customerId;
    private String customerCompanyName;
    private String customerContactPerson;
    private String customerEmail;
    private String customerMobile;
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedEndDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndDate;

    private List<WorkOrderItemResponse> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private String updatedBy;

    private UUID companyId;
    private String companyCode;
}
