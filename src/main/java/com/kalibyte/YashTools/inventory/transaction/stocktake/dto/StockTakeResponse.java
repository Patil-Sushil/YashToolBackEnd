package com.kalibyte.YashTools.inventory.transaction.stocktake.dto;

import com.kalibyte.YashTools.inventory.shared.enums.StockTakeStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTakeResponse {
    private UUID id;
    private String stockTakeNumber;
    
    private StockTakeStatus status;
    private String remarks;
    private List<StockTakeLineResponse> lines;
    
    private LocalDateTime createdAt;
    private String createdBy;
}
