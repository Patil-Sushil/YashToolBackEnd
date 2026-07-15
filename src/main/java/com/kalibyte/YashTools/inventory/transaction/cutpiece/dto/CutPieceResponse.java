package com.kalibyte.YashTools.inventory.transaction.cutpiece.dto;

import com.kalibyte.YashTools.inventory.shared.enums.CutPieceStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CutPieceResponse {
    private UUID id;
    private String code;
    
    private UUID itemId;
    private String itemName;
    private String itemSku;
    
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    
    private BigDecimal remainingLength;
    private CutPieceStatus status;
}
