package com.kalibyte.YashTools.quotation.email;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailAttachment;
import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.entity.enums.EmailPriority;
import com.kalibyte.YashTools.email.util.EmailMimeUtils;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuotationEmailComposer {

    private final CompanyRepository companyRepository;

    public static final String TEMPLATE_QUOTATION = "emails/quotation";

    public EmailMessage compose(QuotationResponse q, byte[] pdfAttachment, List<String> ccList) {

        String companyName = "Yash Tools";
        if (q.getCompanyId() != null) {
            companyName = companyRepository.findById(q.getCompanyId())
                    .map(Company::getName)
                    .orElse("Yash Tools");
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("quotation", q);
        vars.put("items", q.getItems());
        vars.put("customerName", q.getCustomerCompanyName());
        vars.put("contactPerson", q.getCustomerContactPerson());
        vars.put("quotationNo", q.getQuotationNo());
        vars.put("grandTotal", q.getGrandTotal());
        vars.put("validUntil", q.getValidUntil());
        vars.put("companyName", companyName);

        EmailMessage message = EmailMessage.builder()
                .entityType(EmailConstants.OWNER_TYPE_QUOTATION)
                .entityId(q.getQuotationId())
                .companyId(q.getCompanyId())
                .companyCode(q.getCompanyCode())
                .to(q.getCustomerEmail())
                .cc(ccList)
                .subject(buildSubject(q))
                .templateName(TEMPLATE_QUOTATION)
                .templateVariables(vars)
                .priority(Boolean.TRUE.equals(q.getIsUrgent())
                        ? EmailPriority.HIGH : EmailPriority.NORMAL)
                .async(true)
                .build();

        if (pdfAttachment != null && pdfAttachment.length > 0) {
            String filename = "Quotation-" + q.getQuotationNo() + ".pdf";
            message.getAttachments().add(EmailAttachment.builder()
                    .filename(filename)
                    .contentType(EmailMimeUtils.mimeType(".pdf"))
                    .data(pdfAttachment)
                    .description("Quotation PDF")
                    .inline(false)
                    .build());
        }
        return message;
    }

    private String buildSubject(QuotationResponse q) {
        StringBuilder sb = new StringBuilder();
        sb.append("Quotation ").append(q.getQuotationNo());
        if (q.getVersion() != null && q.getVersion() > 1) {
            sb.append(" (v").append(q.getVersion()).append(")");
        }
        if (Boolean.TRUE.equals(q.getIsUrgent())) {
            sb.append(" [URGENT]");
        }
        if (q.getCustomerCompanyName() != null) {
            sb.append(" — ").append(q.getCustomerCompanyName());
        }
        return sb.toString();
    }
}