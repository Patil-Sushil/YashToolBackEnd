package com.kalibyte.YashTools.quotation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationRevisionResponse {
    private UUID id;
    private UUID quotationId;
    private Integer versionNumber;
    private String revisionType;
    private String revisionReason;
    private BigDecimal previousSubtotal;
    private BigDecimal previousDiscount;
    private BigDecimal previousGrandTotal;
    private BigDecimal newSubtotal;
    private BigDecimal newDiscount;
    private BigDecimal newGrandTotal;
    private String previousStatus;
    private String newStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String createdBy;
}