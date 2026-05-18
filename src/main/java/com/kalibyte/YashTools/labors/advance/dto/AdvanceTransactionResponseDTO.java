package com.kalibyte.YashTools.labors.advance.dto;

import com.kalibyte.YashTools.labors.advance.entity.Enum.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceTransactionResponseDTO {
    private Long id;
    private Long laborerId;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String notes;
}
