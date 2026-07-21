package com.kalibyte.YashTools.production.machine.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineResponse {
    private UUID id;
    private String name;
    private String code;
    private String status;
    private UUID companyId;
}
