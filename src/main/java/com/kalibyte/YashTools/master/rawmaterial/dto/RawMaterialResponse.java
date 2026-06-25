package com.kalibyte.YashTools.master.rawmaterial.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialResponse {

    private UUID id;
    private String name;
    private Double rate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}