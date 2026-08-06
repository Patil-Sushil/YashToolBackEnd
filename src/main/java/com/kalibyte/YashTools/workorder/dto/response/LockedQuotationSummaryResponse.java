package com.kalibyte.YashTools.workorder.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight response DTO representing a locked quotation available
 * for work order creation. Used in the create-work-order flow to
 * let the user select which locked quotation to convert.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockedQuotationSummaryResponse {

    private UUID quotationId;
    private String quotationNo;
    private Integer version;
    private String status;

    private UUID customerId;
    private String customerCompanyName;
    private String customerContactPerson;

    private BigDecimal grandTotal;
    private String currency;
    private Integer itemCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockedAt;
    private String lockedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
