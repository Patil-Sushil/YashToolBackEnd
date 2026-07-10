package com.kalibyte.YashTools.workorder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkOrderStatusRequest {

    @NotNull(message = "Status is required")
    private String status;

    private String remarks;
}
