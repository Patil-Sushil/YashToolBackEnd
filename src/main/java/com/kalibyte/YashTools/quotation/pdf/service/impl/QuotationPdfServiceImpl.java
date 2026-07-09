package com.kalibyte.YashTools.quotation.pdf.service.impl;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.service.CompanyBrandingService;
import com.kalibyte.YashTools.customer.service.CustomerService;
import com.kalibyte.YashTools.quotation.dto.response.QuotationItemResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.quotation.pdf.service.QuotationPdfService;
import com.kalibyte.YashTools.quotation.service.QuotationService;
import com.lowagie.text.Image;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.ImageResource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class QuotationPdfServiceImpl implements QuotationPdfService {

    private final QuotationService quotationService;
    private final QuotationSecurityService security;
    private final CompanyBrandingService companyBrandingService;
    private final CustomerService customerService;
    private final TemplateEngine pdfTemplateEngine;

    public QuotationPdfServiceImpl(
            QuotationService quotationService,
            QuotationSecurityService security,
            CompanyBrandingService companyBrandingService,
            CustomerService customerService,
            @Qualifier("pdfTemplateEngine") TemplateEngine pdfTemplateEngine) {
        this.quotationService = quotationService;
        this.security = security;
        this.companyBrandingService = companyBrandingService;
        this.customerService = customerService;
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    @Override
    public byte[] generatePdf(UUID id) {
        security.loadForCurrentCompany(id);
        return generatePdf(quotationService.getById(id));
    }

    @Override
    public byte[] generatePdf(QuotationResponse quotation) {
        log.info("Generating PDF for quotation: {}", quotation.getQuotationNo());

        try {
            Company company = loadCompanyBranding(quotation.getCompanyId());
            String customerAddress = loadCustomerAddress(quotation.getCustomerId());
            List<TemplateItem> templateItems = buildTemplateItems(quotation);

            Context context = buildContext(quotation, company, customerAddress, templateItems);
            String html = pdfTemplateEngine.process("quotation_pdf", context);

            saveDebugHtml(html);
            byte[] pdfBytes = convertHtmlToPdf(html);

            log.info("PDF generated successfully for quotation: {}", quotation.getQuotationNo());
            return pdfBytes;

        } catch (Exception e) {
            log.error("Failed to generate PDF for quotation: {}", quotation.getQuotationNo(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private Company loadCompanyBranding(UUID companyId) {
        if (companyId == null) return null;
        try {
            return companyBrandingService.getBrandingForCompany(companyId);
        } catch (Exception e) {
            log.warn("Could not load company branding: {}", e.getMessage());
            return null;
        }
    }

    private String loadCustomerAddress(UUID customerId) {
        if (customerId == null) return null;
        try {
            var customer = customerService.getCustomerById(customerId);
            if (customer != null) {
                String address = customer.getBillingAddress();
                if (address == null || address.trim().isEmpty()) {
                    address = customer.getDeliveryAddress();
                }
                return address;
            }
        } catch (Exception e) {
            log.warn("Could not load customer address: {}", e.getMessage());
        }
        return null;
    }

    private List<TemplateItem> buildTemplateItems(QuotationResponse quotation) {
        List<TemplateItem> items = new ArrayList<>();
        if (quotation.getItems() != null) {
            for (QuotationItemResponse item : quotation.getItems()) {
                items.add(mapToTemplateItem(item, quotation.getCompanyCode()));
            }
        }
        return items;
    }

    private Context buildContext(QuotationResponse quotation, Company company,
                                 String customerAddress, List<TemplateItem> items) {
        Context context = new Context();
        context.setVariable("companyLogoUrl", company != null ? company.getLogoUrl() : null);
        context.setVariable("companyAddress", company != null ? company.getAddress() : null);
        context.setVariable("companyEmail", company != null ? company.getEmail() : null);
        context.setVariable("companyPhone", company != null ? company.getPhone() : null);
        context.setVariable("customerCompanyName", quotation.getCustomerCompanyName());
        context.setVariable("customerAddress", customerAddress);
        context.setVariable("quotation", quotation);
        context.setVariable("items", items);

        String companyName = "SWARA ENTERPRISES";
        if (company != null) {
            companyName = company.getName();
        } else if ("YT".equals(quotation.getCompanyCode())) {
            companyName = "YASH TOOLS";
        }
        context.setVariable("companyName", companyName);
        context.setVariable("company", company);

        return context;
    }

    private void saveDebugHtml(String html) {
        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("rendered_quotation.html"),
                    html,
                    StandardCharsets.UTF_8
            );
            log.debug("Debug HTML saved to rendered_quotation.html");
        } catch (Exception e) {
            log.warn("Failed to save debug HTML: {}", e.getMessage());
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
        CustomResourceLoader resourceLoader = new CustomResourceLoader(renderer);
        renderer.getSharedContext().setUserAgentCallback(resourceLoader);
        renderer.setDocument(doc, "http://localhost/");
        renderer.layout();
        renderer.createPDF(baos);

        baos.close();
        return baos.toByteArray();
    }

    private TemplateItem mapToTemplateItem(QuotationItemResponse item, String companyCode) {
        String desc = item.getToolName();
        if (desc == null || desc.trim().isEmpty()) {
            desc = item.getItemName();
        }
        if (desc == null) {
            desc = "";
        }

        String code = (companyCode != null ? companyCode : "YT") + item.getLineNumber();

        BigDecimal ratePerPiece = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;

        String discountPercent = "0";
        if (item.getLineSubtotal() != null && item.getLineSubtotal().compareTo(BigDecimal.ZERO) > 0
                && item.getLineDiscountAmount() != null) {
            BigDecimal pct = item.getLineDiscountAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(item.getLineSubtotal(), 0, RoundingMode.HALF_UP);
            discountPercent = pct.toString();
        }

        BigDecimal finalRate = ratePerPiece;
        if (item.getQuantity() != null && item.getQuantity() > 0) {
            BigDecimal lineAmt = item.getLineSubtotal() != null ? item.getLineSubtotal() : BigDecimal.ZERO;
            if (item.getLineDiscountAmount() != null) {
                lineAmt = lineAmt.subtract(item.getLineDiscountAmount());
            }
            finalRate = lineAmt.divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = item.getLineTotal() != null ? item.getLineTotal() : BigDecimal.ZERO;
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0 && item.getQuantity() != null && finalRate != null) {
            totalAmount = finalRate.multiply(BigDecimal.valueOf(item.getQuantity()));
        }

        return TemplateItem.builder()
                .lineNumber(item.getLineNumber())
                .description(desc.toString())
                .code(code)
                .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                .ratePerPiece(ratePerPiece)
                .discountPercent(discountPercent)
                .finalRate(finalRate)
                .totalAmount(totalAmount)
                .build();
    }

    @Data
    @Builder
    public static class TemplateItem {
        private Integer lineNumber;
        private String description;
        private String code;
        private Integer quantity;
        private BigDecimal ratePerPiece;
        private String discountPercent;
        private BigDecimal finalRate;
        private BigDecimal totalAmount;
    }

    private static class CustomResourceLoader extends ITextUserAgent {

        public CustomResourceLoader(ITextRenderer renderer) {
            super(renderer.getOutputDevice());
        }

        @Override
        protected InputStream resolveAndOpenStream(String uri) {
            if (uri == null) return null;

            log.debug("Resolving resource: {}", uri);

            if (uri.startsWith("jar:") || (uri.startsWith("file:") && !uri.contains("static/"))) {
                try {
                    return super.resolveAndOpenStream(uri);
                } catch (Exception e) {
                    log.warn("Cannot load system resource: {}", uri);
                    return null;
                }
            }

            String path = uri;
            if (path.startsWith("http://localhost/")) {
                path = path.substring("http://localhost/".length());
            }
            if (path.startsWith("classpath:")) path = path.substring("classpath:".length());
            if (path.startsWith("file:")) path = path.substring("file:".length());
            while (path.startsWith("/")) path = path.substring(1);

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream(path);

            if (is == null && !path.startsWith("static/")) {
                is = cl.getResourceAsStream("static/" + path);
            }

            if (is == null && !path.startsWith("templates/")) {
                is = cl.getResourceAsStream("templates/" + path);
            }

            if (is != null) {
                log.debug("Successfully loaded resource: {}", path);
            } else {
                log.warn("Resource not found: {} (original URI: {})", path, uri);
            }

            return is;
        }

        @Override
        public ImageResource getImageResource(String uri) {
            log.debug("Loading image resource: {}", uri);

            InputStream is = resolveAndOpenStream(uri);
            if (is != null) {
                try {
                    byte[] bytes = is.readAllBytes();
                    is.close();

                    Image image = Image.getInstance(bytes);
                    org.xhtmlrenderer.pdf.ITextFSImage fsImage = new org.xhtmlrenderer.pdf.ITextFSImage(image);

                    log.debug("Successfully loaded image: {}", uri);
                    return new ImageResource(uri, fsImage);
                } catch (Exception e) {
                    log.error("Failed to load image: {}", uri, e);
                }
            }

            return super.getImageResource(uri);
        }
    }
}