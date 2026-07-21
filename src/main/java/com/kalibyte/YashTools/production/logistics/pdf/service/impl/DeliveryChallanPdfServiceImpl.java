package com.kalibyte.YashTools.production.logistics.pdf.service.impl;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.service.CompanyBrandingService;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;
import com.kalibyte.YashTools.production.logistics.pdf.service.DeliveryChallanPdfService;
import com.kalibyte.YashTools.production.logistics.service.DeliveryChallanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class DeliveryChallanPdfServiceImpl implements DeliveryChallanPdfService {

    private final DeliveryChallanService deliveryChallanService;
    private final CompanyBrandingService companyBrandingService;
    private final TemplateEngine pdfTemplateEngine;

    public DeliveryChallanPdfServiceImpl(
            DeliveryChallanService deliveryChallanService,
            CompanyBrandingService companyBrandingService,
            @Qualifier("pdfTemplateEngine") TemplateEngine pdfTemplateEngine) {
        this.deliveryChallanService = deliveryChallanService;
        this.companyBrandingService = companyBrandingService;
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    @Override
    public byte[] generatePdf(UUID id) {
        DeliveryChallanResponse challan = deliveryChallanService.getChallanById(id);
        return generatePdf(challan);
    }

    @Override
    public byte[] generatePdf(DeliveryChallanResponse challan) {
        log.info("Generating PDF for Delivery Challan: {}", challan.getChallanNo());

        try {
            Company company = null;
            if (challan.getCompanyId() != null) {
                try {
                    company = companyBrandingService.getBrandingForCompany(challan.getCompanyId());
                } catch (Exception e) {
                    log.warn("Could not load company branding: {}", e.getMessage());
                }
            }

            Context context = new Context();
            context.setVariable("challan", challan);
            context.setVariable("companyName", company != null ? company.getName() : "Yash Tools");
            context.setVariable("companyAddress", company != null && company.getAddress() != null ? company.getAddress() : "");
            context.setVariable("companyGst", company != null && company.getGstNumber() != null ? company.getGstNumber() : "N/A");

            String html = pdfTemplateEngine.process("delivery_challan_pdf", context);
            return convertHtmlToPdf(html);

        } catch (Exception e) {
            log.error("Failed to generate PDF for Delivery Challan: {}", challan.getChallanNo(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private byte[] convertHtmlToPdf(String html) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(doc, "http://localhost/");
        renderer.layout();
        renderer.createPDF(baos);

        baos.close();
        return baos.toByteArray();
    }
}
