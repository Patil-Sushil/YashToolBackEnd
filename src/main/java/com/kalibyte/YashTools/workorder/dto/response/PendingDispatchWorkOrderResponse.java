package com.kalibyte.YashTools.workorder.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for pending dispatch work orders — work orders
 * that have items with remaining undispatched quantities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingDispatchWorkOrderResponse {

    private UUID workOrderId;
    private String workOrderNumber;
    private UUID customerId;
    private String customerName;
    private String companyName;
    private List<PendingDispatchItemResponse> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PendingDispatchItemResponse {
        private UUID workOrderItemId;
        private String toolName;
        private Integer orderedQuantity;
        private Integer dispatchedQuantity;
        private Integer remainingQuantity;
        private BigDecimal unitPrice;
        private Double diameter;
        private Double fluteLength;
        private Double shankDiameter;
        private Double overallLength;
    }
}
