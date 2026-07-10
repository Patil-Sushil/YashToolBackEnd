package com.kalibyte.YashTools.master.ratechart.entity;

import com.kalibyte.YashTools.common.enums.ServiceType;
import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;

@Entity
@Table(
    name = "tool_service_rate_masters",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_company_service_code",
            columnNames = {"company_id", "service_code"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Filter(name = "companyFilter", condition = "company_id = :companyId")
public class ToolServiceRateMaster extends BaseCompanyEntity {

    @Column(name = "service_code", nullable = false, length = 50)
    private String serviceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(name = "tool_type", nullable = false, length = 100)
    private String toolType;

    @Column(name = "tool_material", nullable = false, length = 50)
    private String toolMaterial; // e.g. HSS, Carbide

    @Column(name = "diameter_from", nullable = false)
    private Double diameterFrom;

    @Column(name = "diameter_to", nullable = false)
    private Double diameterTo;

    @Column(name = "no_of_flutes")
    private Integer noOfFlutes;

    @Column(name = "profile_type", length = 100)
    private String profileType; // Standard / Ball Nose / Corner Radius / Step / Special

    @Column(name = "base_rate", nullable = false)
    private BigDecimal baseRate;

    @Column(name = "minor_damage_charge")
    private BigDecimal minorDamageCharge;

    @Column(name = "medium_damage_charge")
    private BigDecimal mediumDamageCharge;

    @Column(name = "major_damage_charge")
    private BigDecimal majorDamageCharge;

    @Column(name = "coating_tin")
    private BigDecimal coatingTiN;

    @Column(name = "coating_tialn")
    private BigDecimal coatingTiAlN;

    @Column(name = "coating_alcrn")
    private BigDecimal coatingAlCrN;

    @Column(name = "coating_dlc")
    private BigDecimal coatingDlc;

    @Column(name = "special_geometry_charge")
    private BigDecimal specialGeometryCharge;

    @Column(name = "special_profile_charge")
    private BigDecimal specialProfileCharge;

    @Column(name = "express_delivery_charge")
    private BigDecimal expressDeliveryCharge;

    @Column(name = "standard_delivery_days", nullable = false)
    private Integer standardDeliveryDays;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
