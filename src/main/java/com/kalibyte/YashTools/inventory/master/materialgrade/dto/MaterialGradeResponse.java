package com.kalibyte.YashTools.inventory.master.materialgrade.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialGradeResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
}
