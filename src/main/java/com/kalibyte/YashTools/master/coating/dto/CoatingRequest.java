package com.kalibyte.YashTools.master.coating.dto;

import com.kalibyte.YashTools.common.enums.CoatingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoatingRequest {
    @NotBlank
    private String name;

    @NotNull
    private CoatingType coatingType;

    @NotNull
    private Double rate;
}
