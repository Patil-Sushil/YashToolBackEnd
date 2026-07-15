package com.kalibyte.YashTools.inventory.master.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 150, message = "Item name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Item SKU is required")
    @Size(min = 2, max = 100, message = "SKU must be between 2 and 100 characters")
    private String sku;

    private String description;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private UUID materialGradeId;

    @Builder.Default
    private Boolean active = true;
}
