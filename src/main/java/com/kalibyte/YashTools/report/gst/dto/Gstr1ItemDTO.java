package com.kalibyte.YashTools.report.gst.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gstr1ItemDTO {
    private String invoiceNo;
    private LocalDate invoiceDate;
    private String customerName;
    private String customerGstin;
    private BigDecimal taxableValue;
    private BigDecimal cgstRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstRate;
    private BigDecimal sgstAmount;
    private BigDecimal igstRate;
    private BigDecimal igstAmount;
    private BigDecimal totalInvoiceValue;
    private String status;
}
