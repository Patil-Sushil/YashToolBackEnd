package com.kalibyte.YashTools.production.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryReceiptRequest {
    @NotBlank(message = "Received By signature/name is required")
    private String receivedBy;
}
