package com.kalibyte.YashTools.master.ratechart.dto.response;

import com.kalibyte.YashTools.common.enums.ServiceType;
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
public class ToolServiceRateMasterResponse {
    private UUID id;
    private UUID companyId;
    private String serviceCode;
    private ServiceType serviceType;
    private String toolType;
    private String toolMaterial;
    private Double diameterFrom;
    private Double diameterTo;
    private Integer noOfFlutes;
    private String profileType;
    private BigDecimal baseRate;
    private BigDecimal minorDamageCharge;
    private BigDecimal mediumDamageCharge;
    private BigDecimal majorDamageCharge;
    private BigDecimal coatingTiN;
    private BigDecimal coatingTiAlN;
    private BigDecimal coatingAlCrN;
    private BigDecimal coatingDlc;
    private BigDecimal specialGeometryCharge;
    private BigDecimal specialProfileCharge;
    private BigDecimal expressDeliveryCharge;
    private Integer standardDeliveryDays;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
