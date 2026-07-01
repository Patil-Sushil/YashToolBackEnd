package com.kalibyte.YashTools.quotation.email;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Template variables for the quotation email Thymeleaf template.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationEmailContext {
    private QuotationResponse quotation;
    private Company company;
    private String greeting;
    private String closing;
    private LocalDateTime generatedAt;
    private String portalUrl;
}