package com.kalibyte.YashTools.labors.labor.dto;

import com.kalibyte.YashTools.labors.labor.entity.Enum.LaborRole;
import com.kalibyte.YashTools.labors.labor.entity.Enum.WageType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaborerRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;
    private String phNumber;
    private String address;
    @Email
    private String email;

    @NotNull(message = "Labor role is required")
    private LaborRole role;

    @NotNull(message = "Wage type is required")
    private WageType wageType;

    private BigDecimal dailyWage;
    private BigDecimal pieceRate;
    private BigDecimal hourlyRate;
    private Boolean isActive;
}
