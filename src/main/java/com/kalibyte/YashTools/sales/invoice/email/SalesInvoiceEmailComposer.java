package com.kalibyte.YashTools.sales.invoice.email;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailAttachment;
import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.entity.enums.EmailPriority;
import com.kalibyte.YashTools.email.util.EmailMimeUtils;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SalesInvoiceEmailComposer {

    private final CompanyRepository companyRepository;

    public static final String TEMPLATE_SALES_INVOICE = "emails/sales_invoice";

    public EmailMessage compose(SalesInvoiceResponse invoice, byte[] pdfAttachment, List<String> ccList) {
        String companyName = "Yash Tools";
        if (invoice.getCompanyId() != null) {
            companyName = companyRepository.findById(invoice.getCompanyId())
                    .map(Company::getName)
                    .orElse("Yash Tools");
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("invoice", invoice);
        vars.put("invoiceNo", invoice.getInvoiceNo());
        vars.put("workOrderNo", invoice.getWorkOrderNo());
        vars.put("customerName", invoice.getCustomerName());
        vars.put("totalAmount", invoice.getTotalAmount());
        vars.put("companyName", companyName);

        EmailMessage message = EmailMessage.builder()
                .entityType(EmailConstants.OWNER_TYPE_INVOICE)
                .entityId(invoice.getId())
                .companyId(invoice.getCompanyId())
                .companyCode(invoice.getCompanyCode())
                .to(invoice.getCustomerEmail())
                .cc(ccList)
                .subject("Sales Invoice " + invoice.getInvoiceNo() + " — " + (invoice.getCustomerName() != null ? invoice.getCustomerName() : ""))
                .templateName(TEMPLATE_SALES_INVOICE)
                .templateVariables(vars)
                .priority(EmailPriority.NORMAL)
                .async(true)
                .build();

        if (pdfAttachment != null && pdfAttachment.length > 0) {
            String filename = "SalesInvoice-" + invoice.getInvoiceNo() + ".pdf";
            message.getAttachments().add(EmailAttachment.builder()
                    .filename(filename)
                    .contentType(EmailMimeUtils.mimeType(".pdf"))
                    .data(pdfAttachment)
                    .description("Sales Invoice PDF")
                    .inline(false)
                    .build());
        }
        return message;
    }
}
