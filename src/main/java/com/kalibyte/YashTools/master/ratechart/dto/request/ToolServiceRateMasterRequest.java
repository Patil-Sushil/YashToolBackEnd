package com.kalibyte.YashTools.master.ratechart.dto.request;

import com.kalibyte.YashTools.common.enums.ServiceType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolServiceRateMasterRequest {

    private String serviceCode;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotBlank(message = "Tool type is required")
    @Size(max = 100, message = "Tool type must not exceed 100 characters")
    private String toolType;

    @NotBlank(message = "Tool material is required")
    @Size(max = 50, message = "Tool material must not exceed 50 characters")
    private String toolMaterial; // e.g. HSS, Carbide

    @NotNull(message = "Diameter From is required")
    @Min(value = 0, message = "Diameter From must be positive")
    private Double diameterFrom;

    @NotNull(message = "Diameter To is required")
    @Min(value = 0, message = "Diameter To must be positive")
    private Double diameterTo;

    private Integer noOfFlutes;

    @Size(max = 100, message = "Profile type must not exceed 100 characters")
    private String profileType;

    @NotNull(message = "Base rate is required")
    @DecimalMin(value = "0.0", message = "Base rate must be 0 or more")
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

    @NotNull(message = "Standard delivery days is required")
    @Min(value = 1, message = "Standard delivery days must be at least 1")
    private Integer standardDeliveryDays;

    @Builder.Default
    private Boolean active = true;
}
