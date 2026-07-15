package com.kalibyte.YashTools.inventory.transaction.materialissue.dto;

import com.kalibyte.YashTools.inventory.shared.enums.MaterialIssueStatus;
import com.kalibyte.YashTools.inventory.shared.enums.MaterialIssueType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialIssueResponse {
    private UUID id;
    private String issueNumber;
    
    private UUID itemId;
    private String itemName;
    private String itemSku;
    
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    
    private MaterialIssueType issueType;
    
    private UUID cutPieceId;
    private String cutPieceCode;
    
    private BigDecimal requiredLength;
    private BigDecimal issuedLength;
    
    private UUID newCutPieceId;
    private String newCutPieceCode;
    
    private MaterialIssueStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
}
