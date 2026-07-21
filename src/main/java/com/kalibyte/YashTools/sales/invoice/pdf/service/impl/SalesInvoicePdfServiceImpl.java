package com.kalibyte.YashTools.sales.invoice.pdf.service.impl;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.service.CompanyBrandingService;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;
import com.kalibyte.YashTools.sales.invoice.pdf.service.SalesInvoicePdfService;
import com.kalibyte.YashTools.sales.invoice.service.SalesInvoiceService;
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
public class SalesInvoicePdfServiceImpl implements SalesInvoicePdfService {

    private final SalesInvoiceService salesInvoiceService;
    private final CompanyBrandingService companyBrandingService;
    private final TemplateEngine pdfTemplateEngine;

    public SalesInvoicePdfServiceImpl(
            SalesInvoiceService salesInvoiceService,
            CompanyBrandingService companyBrandingService,
            @Qualifier("pdfTemplateEngine") TemplateEngine pdfTemplateEngine) {
        this.salesInvoiceService = salesInvoiceService;
        this.companyBrandingService = companyBrandingService;
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    @Override
    public byte[] generatePdf(UUID id) {
        SalesInvoiceResponse invoice = salesInvoiceService.getInvoiceById(id);
        return generatePdf(invoice);
    }

    @Override
    public byte[] generatePdf(SalesInvoiceResponse invoice) {
        log.info("Generating PDF for Sales Invoice: {}", invoice.getInvoiceNo());

        try {
            Company company = null;
            if (invoice.getCompanyId() != null) {
                try {
                    company = companyBrandingService.getBrandingForCompany(invoice.getCompanyId());
                } catch (Exception e) {
                    log.warn("Could not load company branding: {}", e.getMessage());
                }
            }

            Context context = new Context();
            context.setVariable("invoice", invoice);
            context.setVariable("companyName", company != null ? company.getName() : "Yash Tools");
            context.setVariable("companyAddress", company != null && company.getAddress() != null ? company.getAddress() : "");
            context.setVariable("companyGst", company != null && company.getGstNumber() != null ? company.getGstNumber() : "N/A");
            context.setVariable("companyBank", company != null && company.getBankName() != null ? company.getBankName() : "N/A");
            context.setVariable("companyAccountNo", company != null && company.getBankAccountNo() != null ? company.getBankAccountNo() : "N/A");
            context.setVariable("companyIfsc", company != null && company.getBankIfsc() != null ? company.getBankIfsc() : "N/A");

            String html = pdfTemplateEngine.process("sales_invoice_pdf", context);
            return convertHtmlToPdf(html);

        } catch (Exception e) {
            log.error("Failed to generate PDF for Sales Invoice: {}", invoice.getInvoiceNo(), e);
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
