package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.customer.repository.CustomerRepository;
import com.kalibyte.YashTools.enquiry.entity.Enquiry;
import com.kalibyte.YashTools.enquiry.repository.EnquiryRepository;
import com.kalibyte.YashTools.quotation.config.QuotationProperties;
import com.kalibyte.YashTools.quotation.constants.QuotationConstants;
import com.kalibyte.YashTools.quotation.dto.request.*;
import com.kalibyte.YashTools.quotation.dto.response.PricingBreakdown;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.QuotationItem;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.exception.*;
import com.kalibyte.YashTools.quotation.mapper.QuotationMapper;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.quotation.service.PricingEngineService;
import com.kalibyte.YashTools.quotation.service.QuotationApprovalService;
import com.kalibyte.YashTools.quotation.service.QuotationRevisionService;
import com.kalibyte.YashTools.quotation.service.QuotationService;
import com.kalibyte.YashTools.quotation.util.PricingFormula;
import com.kalibyte.YashTools.quotation.util.QuotationNumberGenerator;
import com.kalibyte.YashTools.quotation.validator.QuotationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final EnquiryRepository enquiryRepository;
    private final PricingEngineService pricingEngineService;
    private final QuotationRevisionService revisionService;
    private final QuotationApprovalService approvalService;
    private final QuotationMapper quotationMapper;
    private final QuotationSecurityService securityService;
    private final QuotationProperties props;
    private final QuotationValidator validator;

    // ============================================
    // CREATE FROM ENQUIRY
    // ============================================
    @Override
    @Transactional
    public QuotationResponse createFromEnquiry(CreateFromEnquiryRequest req) {
        log.info("Creating quotation from enquiry {}", req.getEnquiryId());

        Enquiry enquiry = enquiryRepository.findById(req.getEnquiryId())
                .orElseThrow(() -> new QuotationNotFoundException("Enquiry: " + req.getEnquiryId()));

        List<QuotationStatus> activeStatuses = List.of(
                QuotationStatus.DRAFT, QuotationStatus.PRICING_READY,
                QuotationStatus.PENDING_APPROVAL, QuotationStatus.APPROVED,
                QuotationStatus.SENT_TO_CUSTOMER, QuotationStatus.CUSTOMER_NEGOTIATION);

        if (quotationRepository.existsBySourceEnquiryIdAndStatusIn(enquiry.getId(), activeStatuses))
            throw new DuplicateQuotationException(
                    "Active quotation already exists for enquiry " + enquiry.getEnquiryNo());

        Quotation quotation = buildSkeleton();
        quotation.setSourceType(QuotationConstants.SOURCE_TYPE_FROM_ENQUIRY);
        quotation.setSourceEnquiry(enquiry);
        quotation.setCustomer(enquiry.getCustomer());
        snapshotCustomer(quotation, enquiry.getCustomer());

        if (req.getValidUntil() != null)
            quotation.setValidUntil(req.getValidUntil().atStartOfDay());
        if (req.getTermsAndConditions() != null)
            quotation.setTermsAndConditions(req.getTermsAndConditions());
        if (req.getPaymentTerms() != null) quotation.setPaymentTerms(req.getPaymentTerms());
        if (req.getDeliveryTerms() != null) quotation.setDeliveryTerms(req.getDeliveryTerms());

        enquiry.setStatus(com.kalibyte.YashTools.enquiry.entity.enums.EnquiryStatus.QUOTED);
        enquiryRepository.save(enquiry);

        applyItems(quotation, req.getItems());
        Quotation saved = quotationRepository.saveAndFlush(quotation);

        revisionService.record(saved, saved,
                com.kalibyte.YashTools.quotation.entity.enums.RevisionType.INITIAL,
                "Initial quotation from enquiry " + enquiry.getEnquiryNo());

        log.info("Quotation {} created from enquiry {}",
                saved.getQuotationNo(), enquiry.getEnquiryNo());
        return quotationMapper.toResponse(saved);
    }

    // ============================================
    // CREATE DIRECT
    // ============================================
    @Override
    @Transactional
    public QuotationResponse createDirect(CreateDirectQuotationRequest req) {
        log.info("Creating direct quotation for customer {}", req.getCustomerId());

        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new QuotationNotFoundException("Customer: " + req.getCustomerId()));

        Quotation quotation = buildSkeleton();
        quotation.setSourceType(QuotationConstants.SOURCE_TYPE_DIRECT);
        quotation.setCustomer(customer);
        snapshotCustomer(quotation, customer);
        quotation.setIsUrgent(Boolean.TRUE.equals(req.getIsUrgent()));
        quotation.setRemarks(req.getRemarks());
        quotation.setInternalNotes(req.getInternalNotes());
        quotation.setTermsAndConditions(req.getTermsAndConditions());
        quotation.setPaymentTerms(req.getPaymentTerms());
        quotation.setDeliveryTerms(req.getDeliveryTerms());
        if (req.getValidUntil() != null)
            quotation.setValidUntil(req.getValidUntil().atStartOfDay());

        applyItems(quotation, req.getItems());
        Quotation saved = quotationRepository.saveAndFlush(quotation);

        revisionService.record(saved, saved,
                com.kalibyte.YashTools.quotation.entity.enums.RevisionType.INITIAL,
                "Direct quotation");
        return quotationMapper.toResponse(saved);
    }

    // ============================================
    // READ
    // ============================================
    @Override
    @Transactional(readOnly = true)
    public QuotationResponse getById(UUID id) {
        return quotationMapper.toResponse(securityService.loadForCurrentCompany(id));
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationResponse getByNumber(String qNo) {
        Quotation q = quotationRepository.findByQuotationNo(qNo)
                .orElseThrow(() -> new QuotationNotFoundException(qNo));
        return quotationMapper.toResponse(securityService.loadForCurrentCompany(q.getId()));
    }

    // ============================================
    // UPDATE DRAFT
    // ============================================
    @Override
    @Transactional
    public QuotationResponse updateDraft(UUID id, UpdateQuotationRequest req) {
        Quotation q = securityService.loadForCurrentCompany(id);
        if (!q.isEditable())
            throw new QuotationStateException("Cannot edit in status " + q.getStatus());

        if (req.getValidUntil() != null)
            q.setValidUntil(req.getValidUntil().atStartOfDay());
        if (req.getRemarks() != null) q.setRemarks(req.getRemarks());
        if (req.getTermsAndConditions() != null) q.setTermsAndConditions(req.getTermsAndConditions());
        if (req.getPaymentTerms() != null) q.setPaymentTerms(req.getPaymentTerms());
        if (req.getDeliveryTerms() != null) q.setDeliveryTerms(req.getDeliveryTerms());

        if (req.getDiscountPercentage() != null)
            applyDiscountChange(q, req.getDiscountPercentage());

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            q.getItems().clear();
            applyItems(q, req.getItems());
        }
        return quotationMapper.toResponse(quotationRepository.saveAndFlush(q));
    }

    // ============================================
    // REVISE
    // ============================================
    @Override
    @Transactional
    public QuotationResponse revise(ReviseQuotationRequest req) {
        return revisionService.createRevision(req);
    }

    // ============================================
    // LOCK FINAL
    // ============================================
    @Override
    @Transactional
    public QuotationResponse lockFinal(UUID id) {
        Quotation q = securityService.loadForCurrentCompany(id);
        if (q.getStatus() != QuotationStatus.CUSTOMER_APPROVED)
            throw new QuotationStateException(
                    "Only CUSTOMER_APPROVED can lock. Current: " + q.getStatus());

        q.setIsLocked(true);
        q.setLockedAt(LocalDateTime.now());
        q.setLockedBy(securityService.currentUsername());
        q.setStatus(QuotationStatus.LOCKED);
        return quotationMapper.toResponse(quotationRepository.saveAndFlush(q));
    }

    // ============================================
    // CANCEL
    // ============================================
    @Override
    @Transactional
    public QuotationResponse cancel(UUID id, String reason) {
        Quotation q = securityService.loadForCurrentCompany(id);
        if (q.isTerminal())
            throw new QuotationStateException("Terminal: " + q.getStatus());

        q.setStatus(QuotationStatus.CANCELLED);
        q.setInternalNotes((q.getInternalNotes() == null ? "" : q.getInternalNotes())
                + "\n[CANCELLED] " + LocalDateTime.now() + " - " + reason);
        return quotationMapper.toResponse(quotationRepository.saveAndFlush(q));
    }

    // ============================================
    // LIST
    // ============================================
    @Override
    @Transactional(readOnly = true)
    public Page<QuotationResponse> listForCurrentCompany(String status, Pageable pageable) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        return quotationRepository.findRootQuotations(companyId, pageable)
                .map(quotationMapper::toResponse);
    }

    // ============================================
    // HELPERS
    // ============================================

    private Quotation buildSkeleton() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();
        Company company = Company.builder().id(companyId).code(companyCode).build();

        long seq = quotationRepository.count() + 1;
        String quotationNo = QuotationNumberGenerator.build(seq);

        Quotation q = Quotation.builder()
                .quotationNo(quotationNo)
                .version(1)
                .status(QuotationStatus.DRAFT)
                .isLocked(false)
                .validUntil(LocalDateTime.now().plusDays(props.getDefaultValidityDays()))
                .revisionCount(0)
                .currency("INR")
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .discountPercentage(BigDecimal.ZERO)
                .taxableAmount(BigDecimal.ZERO)
                .cgstAmount(BigDecimal.ZERO)
                .sgstAmount(BigDecimal.ZERO)
                .igstAmount(BigDecimal.ZERO)
                .totalTax(BigDecimal.ZERO)
                .grandTotal(BigDecimal.ZERO)
                .build();
        q.setCompany(company);
        return q;
    }

    private void applyItems(Quotation q, List<QuotationItemRequest> items) {
        validator.validateItems(items);
        BigDecimal subtotal = BigDecimal.ZERO;
        int line = 1;
        for (QuotationItemRequest req : items) {
            PricingBreakdown b = pricingEngineService.priceItem(req);
            QuotationItem item = QuotationItem.builder()
                    .lineNumber(line++)
                    .orderType(req.getOrderType())
                    .toolName(req.getToolName())
                    .itemName(req.getItemName())
                    .quantity(req.getQuantity())
                    .trial(Boolean.TRUE.equals(req.getTrial()))
                    .itemRemarks(req.getRemarks())
                    .drawingReference(req.getDrawingReference())
                    .overallLength(req.getOverallLength())
                    .materialGrade(req.getMaterialGrade())
                    .rateChartItem(b.getRateChartItem())
                    .rateChartGrade(req.getMaterialGrade() != null ? req.getMaterialGrade().name() : null)
                    .ratePerUnit(b.getRatePerUnit())
                    .standardRodLength(BigDecimal.valueOf(b.getStandardRodLengthMm()))
                    .actualLengthUsed(BigDecimal.valueOf(b.getActualLengthMm()))
                    .userMultiplier(b.getUserMultiplier())
                    .basePrice(b.getBasePrice())
                    .multipliedPrice(b.getMultipliedPrice())
                    .unitPrice(b.getUnitPrice())
                    .lineSubtotal(b.getLineSubtotal())
                    .lineTaxableAmount(b.getLineSubtotal())
                    .lineTotal(b.getLineSubtotal())
                    .rateSourceTable(b.getRateSourceTable())
                    .rateFetchedAt(LocalDateTime.now())
                    .build();

            if (req.getSpecs() != null) {
                item.setMaterialType(req.getSpecs().getMaterialType() != null
                        ? req.getSpecs().getMaterialType().name() : null);
                item.setCoatingRequired(Boolean.TRUE.equals(req.getSpecs().getCoatingRequired()));
                item.setCoatingType(req.getSpecs().getCoatingType() != null
                        ? req.getSpecs().getCoatingType().name() : null);
                item.setResharpeningType(req.getSpecs().getResharpeningType() != null
                        ? req.getSpecs().getResharpeningType().name() : null);
                item.setDiameter(req.getSpecs().getDiameter());
                item.setFluteLength(req.getSpecs().getFluteLength());
                item.setShankDiameter(req.getSpecs().getShankDiameter());
                item.setTechnicalNotes(req.getSpecs().getTechnicalNotes());
            }
            q.addItem(item);
            subtotal = subtotal.add(b.getLineSubtotal());
        }
        q.setSubtotal(subtotal);
        applyTaxes(q);
    }

    private void applyTaxes(Quotation q) {
        BigDecimal taxable = q.getSubtotal().subtract(q.getDiscountAmount());
        if (taxable.compareTo(BigDecimal.ZERO) < 0) taxable = BigDecimal.ZERO;
        q.setTaxableAmount(taxable);

        BigDecimal cgst = PricingFormula.computeTax(taxable,
                BigDecimal.valueOf(props.getDefaultCgstPercentage()));
        BigDecimal sgst = PricingFormula.computeTax(taxable,
                BigDecimal.valueOf(props.getDefaultSgstPercentage()));

        q.setCgstAmount(cgst);
        q.setSgstAmount(sgst);
        q.setIgstAmount(BigDecimal.ZERO);
        q.setTotalTax(cgst.add(sgst));
        q.setGrandTotal(taxable.add(q.getTotalTax()).setScale(2, RoundingMode.HALF_UP));

        if (q.getStatus() == QuotationStatus.DRAFT)
            q.setStatus(QuotationStatus.PRICING_READY);
    }

    private void applyDiscountChange(Quotation q, BigDecimal pct) {
        if (pct.compareTo(BigDecimal.ZERO) < 0
                || pct.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new PricingException("Discount % must be 0..100");

        BigDecimal amount = q.getSubtotal().multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        q.setDiscountAmount(amount);
        q.setDiscountPercentage(pct);

        if (approvalService.requiresApproval(pct)) {
            q.setStatus(QuotationStatus.PENDING_APPROVAL);
            approvalService.requestApproval(q.getId(), pct);
        } else {
            q.setStatus(QuotationStatus.APPROVED);
        }
        applyTaxes(q);
    }

    private void snapshotCustomer(Quotation q, Customer c) {
        q.setCustomerCompanyName(c.getCompanyName());
        q.setCustomerContactPerson(c.getCustomerName());
        q.setCustomerEmail(c.getEmail());
        q.setCustomerMobile(c.getMobileNumber());
    }
}