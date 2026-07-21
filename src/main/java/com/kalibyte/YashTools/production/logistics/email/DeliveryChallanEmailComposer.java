package com.kalibyte.YashTools.production.logistics.email;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailAttachment;
import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.entity.enums.EmailPriority;
import com.kalibyte.YashTools.email.util.EmailMimeUtils;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DeliveryChallanEmailComposer {

    private final CompanyRepository companyRepository;

    public static final String TEMPLATE_DELIVERY_CHALLAN = "emails/delivery_challan";

    public EmailMessage compose(DeliveryChallanResponse challan, byte[] pdfAttachment, List<String> ccList) {
        String companyName = "Yash Tools";
        if (challan.getCompanyId() != null) {
            companyName = companyRepository.findById(challan.getCompanyId())
                    .map(Company::getName)
                    .orElse("Yash Tools");
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("challan", challan);
        vars.put("challanNo", challan.getChallanNo());
        vars.put("workOrderNo", challan.getWorkOrderNo());
        vars.put("customerName", challan.getCustomerName());
        vars.put("vehicleNo", challan.getVehicleNo() != null ? challan.getVehicleNo() : "N/A");
        vars.put("companyName", companyName);

        EmailMessage message = EmailMessage.builder()
                .entityType(EmailConstants.OWNER_TYPE_DELIVERY_CHALLAN)
                .entityId(challan.getId())
                .companyId(challan.getCompanyId())
                .companyCode(challan.getCompanyCode())
                .to(challan.getCustomerEmail())
                .cc(ccList)
                .subject("Delivery Challan " + challan.getChallanNo() + " — " + (challan.getCustomerName() != null ? challan.getCustomerName() : ""))
                .templateName(TEMPLATE_DELIVERY_CHALLAN)
                .templateVariables(vars)
                .priority(EmailPriority.NORMAL)
                .async(true)
                .build();

        if (pdfAttachment != null && pdfAttachment.length > 0) {
            String filename = "DeliveryChallan-" + challan.getChallanNo() + ".pdf";
            message.getAttachments().add(EmailAttachment.builder()
                    .filename(filename)
                    .contentType(EmailMimeUtils.mimeType(".pdf"))
                    .data(pdfAttachment)
                    .description("Delivery Challan PDF")
                    .inline(false)
                    .build());
        }
        return message;
    }
}
