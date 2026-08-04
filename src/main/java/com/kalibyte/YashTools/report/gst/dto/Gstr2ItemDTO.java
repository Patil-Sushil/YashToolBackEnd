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
public class Gstr2ItemDTO {
    private String invoiceNumber;
    private String supplierInvoiceNumber;
    private LocalDate invoiceDate;
    private String vendorName;
    private String vendorGstin;
    private BigDecimal taxableValue;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalGstAmount;
    private BigDecimal totalInvoiceValue;
    private String status;
}
