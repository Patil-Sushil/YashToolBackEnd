package com.kalibyte.YashTools.inventory.master.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Category code is required")
    @Size(min = 2, max = 50, message = "Category code must be between 2 and 50 characters")
    private String code;

    private String description;
}
