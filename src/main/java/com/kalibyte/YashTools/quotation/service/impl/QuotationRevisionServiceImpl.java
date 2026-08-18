package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.quotation.dto.request.QuotationItemRequest;
import com.kalibyte.YashTools.quotation.dto.request.ReviseQuotationRequest;
import com.kalibyte.YashTools.quotation.dto.response.PricingBreakdown;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationRevisionResponse;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.QuotationItem;
import com.kalibyte.YashTools.quotation.entity.QuotationRevision;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.entity.enums.RevisionType;
import com.kalibyte.YashTools.quotation.exception.QuotationStateException;
import com.kalibyte.YashTools.quotation.mapper.QuotationMapper;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.quotation.repository.QuotationRevisionRepository;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.quotation.service.PricingEngineService;
import com.kalibyte.YashTools.quotation.service.QuotationRevisionService;
import com.kalibyte.YashTools.quotation.service.QuotationApprovalService;
import com.kalibyte.YashTools.quotation.util.PricingFormula;
import com.kalibyte.YashTools.quotation.util.QuotationNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationRevisionServiceImpl implements QuotationRevisionService {

    private final QuotationRepository quotationRepository;
    private final QuotationRevisionRepository revisionRepository;
    private final QuotationMapper mapper;
    private final QuotationSecurityService security;
    private final PricingEngineService pricingEngine;
    private final QuotationApprovalService approvalService;

    @Override
    @Transactional
    public QuotationResponse createRevision(ReviseQuotationRequest req) {
        Quotation parent = security.loadForCurrentCompany(req.getParentQuotationId());
        if (parent.getStatus() == QuotationStatus.LOCKED 
                || parent.getStatus() == QuotationStatus.CANCELLED 
                || parent.getStatus() == QuotationStatus.EXPIRED) {
            throw new QuotationStateException("Cannot revise quotation in final status: " + parent.getStatus());
        }

        Quotation rootParent = parent;
        while (rootParent.getParentQuotation() != null) {
            rootParent = rootParent.getParentQuotation();
        }

        String baseQuotationNo = QuotationNumberGenerator.getBaseQuotationNo(rootParent.getQuotationNo());

        // Dynamically find the next available revision suffix number by checking database presence
        int nextRevisionNumber = rootParent.getRevisionCount() + 1;
        while (quotationRepository.findByQuotationNo(QuotationNumberGenerator.buildRevisionNumber(baseQuotationNo, nextRevisionNumber)).isPresent()) {
            nextRevisionNumber++;
        }

        rootParent.setRevisionCount(nextRevisionNumber);
        if (!rootParent.getId().equals(parent.getId())) {
            quotationRepository.saveAndFlush(rootParent);
        }

        Company companyRef = Company.builder()
                .id(parent.getCompany().getId())
                .code(parent.getCompany().getCode())
                .build();
        Quotation revised = Quotation.builder()
                .quotationNo(QuotationNumberGenerator.buildRevisionNumber(baseQuotationNo, nextRevisionNumber))
                .version(parent.getVersion() + 1)
                .parentQuotation(parent)
                .sourceType(parent.getSourceType())
                .sourceEnquiry(parent.getSourceEnquiry())
                .customer(parent.getCustomer())
                .customerCompanyName(parent.getCustomerCompanyName())
                .customerContactPerson(parent.getCustomerContactPerson())
                .customerEmail(parent.getCustomerEmail())
                .customerMobile(parent.getCustomerMobile())
                .status(QuotationStatus.DRAFT)
                .isLocked(false)
                .isUrgent(parent.getIsUrgent())
                .validUntil(parent.getValidUntil())
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
                .termsAndConditions(req.getTermsAndConditions() != null
                        ? req.getTermsAndConditions() : parent.getTermsAndConditions())
                .build();
        revised.setCompany(companyRef);
        if (req.getItems() != null && !req.getItems().isEmpty())
            applyItemsWithPricing(revised, req.getItems());
        else
            carryOverItems(parent, revised);

        revised.setDiscountPercentage(req.getDiscountPercentage() != null
                ? req.getDiscountPercentage() : parent.getDiscountPercentage());

        parent.setRevisionRequired(false);
        parent.setLastRevisionAt(LocalDateTime.now());
        parent.setStatus(QuotationStatus.CUSTOMER_NEGOTIATION);
        quotationRepository.saveAndFlush(parent);

        Quotation saved = quotationRepository.saveAndFlush(revised);
        recalculateTotals(saved);
        saved = quotationRepository.saveAndFlush(saved);

        if (saved.getStatus() == QuotationStatus.PENDING_APPROVAL) {
            approvalService.requestApproval(saved.getId(), saved.getDiscountPercentage());
        }

        RevisionType type = RevisionType.CUSTOMER_FEEDBACK;
        if (req.getRevisionType() != null) {
            try { type = RevisionType.valueOf(req.getRevisionType()); }
            catch (IllegalArgumentException ignored) { }
        }
        record(parent, saved, type, req.getRevisionReason());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationRevisionResponse> getRevisionHistory(UUID id) {
        return revisionRepository.findByQuotationIdOrderByVersionNumberDesc(id).stream()
                .map(this::toRevisionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationResponse> getRevisedQuotations(UUID id) {
        Quotation q = security.loadForCurrentCompany(id);
        String baseNo = QuotationNumberGenerator.getBaseQuotationNo(q.getQuotationNo());
        UUID companyId = q.getCompany().getId();
        List<Quotation> revisions = quotationRepository.findRevisionsByBaseQuotationNo(companyId, baseNo);
        return revisions.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationResponse> getRevisedQuotationsByNumber(String quotationNo) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String baseNo = QuotationNumberGenerator.getBaseQuotationNo(quotationNo);
        List<Quotation> revisions = quotationRepository.findRevisionsByBaseQuotationNo(companyId, baseNo);
        return revisions.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.kalibyte.YashTools.quotation.dto.response.QuotationFamilyResponse getRevisionChain(UUID id) {
        Quotation q = security.loadForCurrentCompany(id);
        String baseNo = QuotationNumberGenerator.getBaseQuotationNo(q.getQuotationNo());
        UUID companyId = q.getCompany().getId();
        return buildFamilyResponse(companyId, baseNo);
    }

    @Override
    @Transactional(readOnly = true)
    public com.kalibyte.YashTools.quotation.dto.response.QuotationFamilyResponse getRevisionChainByNumber(String quotationNo) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String baseNo = QuotationNumberGenerator.getBaseQuotationNo(quotationNo);
        return buildFamilyResponse(companyId, baseNo);
    }

    private com.kalibyte.YashTools.quotation.dto.response.QuotationFamilyResponse buildFamilyResponse(UUID companyId, String baseNo) {
        List<Quotation> family = quotationRepository.findFamilyByBaseQuotationNo(companyId, baseNo);
        if (family.isEmpty()) {
            throw new com.kalibyte.YashTools.quotation.exception.QuotationNotFoundException("Quotation family not found for: " + baseNo);
        }

        Quotation root = family.stream()
                .filter(q -> q.getParentQuotation() == null || q.getQuotationNo().equals(baseNo))
                .findFirst()
                .orElse(family.get(0));

        List<QuotationResponse> revisions = family.stream()
                .filter(q -> !q.getId().equals(root.getId()))
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        Quotation latest = family.get(family.size() - 1);

        return com.kalibyte.YashTools.quotation.dto.response.QuotationFamilyResponse.builder()
                .rootQuotationId(root.getId())
                .rootQuotationNo(root.getQuotationNo())
                .rootQuotation(mapper.toResponse(root))
                .revisions(revisions)
                .totalRevisions(revisions.size())
                .latestRevision(mapper.toResponse(latest))
                .build();
    }

    @Override
    @Transactional
    public void record(Quotation prev, Quotation revised, RevisionType type, String reason) {
        QuotationRevision rev = QuotationRevision.builder()
                .quotation(revised)
                .versionNumber(revised.getVersion())
                .revisionType(type.name())
                .revisionReason(reason)
                .previousSubtotal(prev.getSubtotal())
                .previousDiscount(prev.getDiscountAmount())
                .previousGrandTotal(prev.getGrandTotal())
                .newSubtotal(revised.getSubtotal())
                .newDiscount(revised.getDiscountAmount())
                .newGrandTotal(revised.getGrandTotal())
                .previousStatus(prev.getStatus().name())
                .newStatus(revised.getStatus().name())
                .build();
        revised.addRevision(rev);
        revisionRepository.save(rev);
    }

    // ============== private helpers ==============

    private void applyItemsWithPricing(Quotation q, List<QuotationItemRequest> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int line = 1;
        for (QuotationItemRequest req : items) {
            PricingBreakdown b = pricingEngine.priceItem(req);
            QuotationItem item = QuotationItem.builder()
                    .lineNumber(line++)
                    .orderType(req.getOrderType())
                    .toolName(req.getToolName())
                    .itemName(req.getItemName())
                    .quantity(req.getQuantity())
                    .trial(Boolean.TRUE.equals(req.getTrial()))
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
                    .coatingCharge(b.getCoatingCharge() != null ? b.getCoatingCharge() : BigDecimal.ZERO)
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
                item.setDamageLevel(req.getSpecs().getDamageLevel());
                item.setSpecialGeometry(Boolean.TRUE.equals(req.getSpecs().getSpecialGeometry()));
                item.setSpecialProfile(Boolean.TRUE.equals(req.getSpecs().getSpecialProfile()));
                item.setExpressDelivery(Boolean.TRUE.equals(req.getSpecs().getExpressDelivery()));
            }
            q.addItem(item);
            subtotal = subtotal.add(b.getLineSubtotal());
        }
        q.setSubtotal(subtotal);
    }

    private void carryOverItems(Quotation from, Quotation to) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int line = 1;
        for (QuotationItem src : from.getItems()) {
            QuotationItem copy = QuotationItem.builder()
                    .lineNumber(line++)
                    .orderType(src.getOrderType())
                    .toolName(src.getToolName())
                    .itemName(src.getItemName())
                    .quantity(src.getQuantity())
                    .trial(src.getTrial())
                    .itemRemarks(src.getItemRemarks())
                    .drawingReference(src.getDrawingReference())
                    .overallLength(src.getOverallLength())
                    .materialType(src.getMaterialType())
                    .materialGrade(src.getMaterialGrade())
                    .coatingRequired(src.getCoatingRequired())
                    .coatingType(src.getCoatingType())
                    .resharpeningType(src.getResharpeningType())
                    .diameter(src.getDiameter())
                    .fluteLength(src.getFluteLength())
                    .shankDiameter(src.getShankDiameter())
                    .technicalNotes(src.getTechnicalNotes())
                    .damageLevel(src.getDamageLevel())
                    .specialGeometry(src.getSpecialGeometry())
                    .specialProfile(src.getSpecialProfile())
                    .expressDelivery(src.getExpressDelivery())
                    .rateChartItem(src.getRateChartItem())
                    .rateChartGrade(src.getRateChartGrade())
                    .ratePerUnit(src.getRatePerUnit())
                    .standardRodLength(src.getStandardRodLength())
                    .actualLengthUsed(src.getActualLengthUsed())
                    .userMultiplier(src.getUserMultiplier())
                    .basePrice(src.getBasePrice())
                    .multipliedPrice(src.getMultipliedPrice())
                    .coatingCharge(src.getCoatingCharge())
                    .unitPrice(src.getUnitPrice())
                    .lineSubtotal(src.getLineSubtotal())
                    .lineTaxableAmount(src.getLineTaxableAmount())
                    .lineTotal(src.getLineTotal())
                    .rateSourceTable(src.getRateSourceTable())
                    .rateFetchedAt(LocalDateTime.now())
                    .build();
            to.addItem(copy);
            subtotal = subtotal.add(src.getLineSubtotal());
        }
        to.setSubtotal(subtotal);
    }

    private void recalculateTotals(Quotation q) {
        BigDecimal discountPercentage = q.getDiscountPercentage() != null ? q.getDiscountPercentage() : BigDecimal.ZERO;
        BigDecimal discountAmount = q.getSubtotal().multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        q.setDiscountAmount(discountAmount);

        BigDecimal taxable = q.getSubtotal().subtract(discountAmount);
        if (taxable.compareTo(BigDecimal.ZERO) < 0) taxable = BigDecimal.ZERO;
        q.setTaxableAmount(taxable);

        BigDecimal cgst = PricingFormula.computeTax(taxable, BigDecimal.valueOf(9.0));
        BigDecimal sgst = PricingFormula.computeTax(taxable, BigDecimal.valueOf(9.0));
        q.setCgstAmount(cgst);
        q.setSgstAmount(sgst);
        q.setTotalTax(cgst.add(sgst));
        q.setGrandTotal(taxable.add(q.getTotalTax()).setScale(2, RoundingMode.HALF_UP));

        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0 && approvalService.requiresApproval(discountPercentage)) {
            q.setStatus(QuotationStatus.PENDING_APPROVAL);
        } else {
            q.setStatus(QuotationStatus.PRICING_READY);
        }
    }

    private QuotationRevisionResponse toRevisionResponse(QuotationRevision r) {
        return QuotationRevisionResponse.builder()
                .id(r.getId())
                .quotationId(r.getQuotation().getId())
                .versionNumber(r.getVersionNumber())
                .revisionType(r.getRevisionType())
                .revisionReason(r.getRevisionReason())
                .previousSubtotal(r.getPreviousSubtotal())
                .previousDiscount(r.getPreviousDiscount())
                .previousGrandTotal(r.getPreviousGrandTotal())
                .newSubtotal(r.getNewSubtotal())
                .newDiscount(r.getNewDiscount())
                .newGrandTotal(r.getNewGrandTotal())
                .previousStatus(r.getPreviousStatus())
                .newStatus(r.getNewStatus())
                .createdAt(r.getCreatedAt())
                .createdBy(r.getCreatedBy())
                .build();
    }
}