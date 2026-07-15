package com.kalibyte.YashTools.inventory.transaction.stocktake.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTakeRequest {

    private String remarks;

    @NotEmpty(message = "Stock take lines cannot be empty")
    @Valid
    private List<StockTakeLineRequest> lines;
}
