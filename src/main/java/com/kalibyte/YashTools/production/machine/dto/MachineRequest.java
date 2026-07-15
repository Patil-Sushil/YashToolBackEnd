package com.kalibyte.YashTools.production.machine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineRequest {

    @NotBlank(message = "Machine name is required")
    private String name;

    @NotBlank(message = "Machine code is required")
    private String code;

    private String status;
}
