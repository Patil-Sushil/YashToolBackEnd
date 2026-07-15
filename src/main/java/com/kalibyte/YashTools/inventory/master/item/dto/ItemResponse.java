package com.kalibyte.YashTools.inventory.master.item.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private UUID id;
    private String name;
    private String sku;
    private String description;
    
    private UUID categoryId;
    private String categoryName;
    private String categoryCode;
    
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    
    private Boolean active;
}
