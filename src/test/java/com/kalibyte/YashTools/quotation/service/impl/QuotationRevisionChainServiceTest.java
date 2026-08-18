package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.quotation.dto.response.QuotationFamilyResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.mapper.QuotationMapper;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.quotation.repository.QuotationRevisionRepository;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.quotation.service.PricingEngineService;
import com.kalibyte.YashTools.quotation.service.QuotationApprovalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuotationRevisionChainServiceTest {

    @Mock private QuotationRepository quotationRepository;
    @Mock private QuotationRevisionRepository revisionRepository;
    @Mock private QuotationSecurityService security;
    @Mock private PricingEngineService pricingEngine;
    @Mock private QuotationApprovalService approvalService;

    private QuotationMapper mapper;
    private QuotationRevisionServiceImpl revisionService;

    private UUID companyId;
    private Company company;
    private Customer customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = Mappers.getMapper(QuotationMapper.class);
        revisionService = new QuotationRevisionServiceImpl(
                quotationRepository, revisionRepository, mapper, security, pricingEngine, approvalService
        );

        companyId = UUID.randomUUID();
        CompanyContextHolder.setCompanyId(companyId);
        CompanyContextHolder.setCompanyCode("YT");
        company = Company.builder().id(companyId).code("YT").build();

        customer = Customer.builder()
                .customerName("Acme Corp Contact")
                .companyName("Acme Corp")
                .email("acme@test.com")
                .mobileNumber("9999999999")
                .build();
        customer.setId(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Should fetch all revised quotations and family revision chain")
    void testGetRevisedQuotationsAndFamilyChain() {
        UUID rootId = UUID.randomUUID();
        UUID r1Id = UUID.randomUUID();
        UUID r2Id = UUID.randomUUID();

        Quotation root = Quotation.builder()
                .quotationNo("YT-QT-2026-000001")
                .version(1)
                .status(QuotationStatus.CUSTOMER_NEGOTIATION)
                .customer(customer)
                .sourceType("DIRECT")
                .validUntil(LocalDateTime.now().plusDays(30))
                .subtotal(BigDecimal.valueOf(1000))
                .grandTotal(BigDecimal.valueOf(1180))
                .items(new ArrayList<>())
                .build();
        root.setId(rootId);
        root.setCompany(company);

        Quotation rev1 = Quotation.builder()
                .quotationNo("YT-QT-2026-000001-R1")
                .version(2)
                .parentQuotation(root)
                .status(QuotationStatus.CUSTOMER_NEGOTIATION)
                .customer(customer)
                .sourceType("DIRECT")
                .validUntil(LocalDateTime.now().plusDays(30))
                .subtotal(BigDecimal.valueOf(950))
                .grandTotal(BigDecimal.valueOf(1121))
                .items(new ArrayList<>())
                .build();
        rev1.setId(r1Id);
        rev1.setCompany(company);

        Quotation rev2 = Quotation.builder()
                .quotationNo("YT-QT-2026-000001-R2")
                .version(3)
                .parentQuotation(rev1)
                .status(QuotationStatus.CUSTOMER_APPROVED)
                .customer(customer)
                .sourceType("DIRECT")
                .validUntil(LocalDateTime.now().plusDays(30))
                .subtotal(BigDecimal.valueOf(900))
                .grandTotal(BigDecimal.valueOf(1062))
                .items(new ArrayList<>())
                .build();
        rev2.setId(r2Id);
        rev2.setCompany(company);

        when(security.loadForCurrentCompany(rootId)).thenReturn(root);
        when(security.loadForCurrentCompany(r1Id)).thenReturn(rev1);
        when(security.loadForCurrentCompany(r2Id)).thenReturn(rev2);

        when(quotationRepository.findRevisionsByBaseQuotationNo(companyId, "YT-QT-2026-000001"))
                .thenReturn(List.of(rev1, rev2));
        when(quotationRepository.findFamilyByBaseQuotationNo(companyId, "YT-QT-2026-000001"))
                .thenReturn(List.of(root, rev1, rev2));

        // 1. Test getRevisedQuotations
        List<QuotationResponse> revisions = revisionService.getRevisedQuotations(rootId);
        assertNotNull(revisions);
        assertEquals(2, revisions.size());
        assertEquals("YT-QT-2026-000001-R1", revisions.get(0).getQuotationNo());
        assertEquals("YT-QT-2026-000001-R2", revisions.get(1).getQuotationNo());
        assertTrue(revisions.get(0).getIsRevision());
        assertEquals(1, revisions.get(0).getRevisionNumber());
        assertEquals(2, revisions.get(1).getRevisionNumber());
        assertEquals("YT-QT-2026-000001", revisions.get(0).getRootQuotationNo());

        // 2. Test getRevisionChain by Root ID
        QuotationFamilyResponse chain = revisionService.getRevisionChain(rootId);
        assertNotNull(chain);
        assertEquals("YT-QT-2026-000001", chain.getRootQuotationNo());
        assertEquals(rootId, chain.getRootQuotationId());
        assertEquals("YT-QT-2026-000001", chain.getRootQuotation().getQuotationNo());
        assertEquals(2, chain.getTotalRevisions());
        assertEquals(2, chain.getRevisions().size());
        assertEquals("YT-QT-2026-000001-R2", chain.getLatestRevision().getQuotationNo());

        // 3. Test getRevisionChain by Child Revision ID (R1)
        QuotationFamilyResponse chainFromChild = revisionService.getRevisionChain(r1Id);
        assertNotNull(chainFromChild);
        assertEquals("YT-QT-2026-000001", chainFromChild.getRootQuotationNo());
        assertEquals(2, chainFromChild.getTotalRevisions());
        assertEquals("YT-QT-2026-000001-R2", chainFromChild.getLatestRevision().getQuotationNo());

        // 4. Test getRevisionChain by Quotation Number String
        QuotationFamilyResponse chainByNumber = revisionService.getRevisionChainByNumber("YT-QT-2026-000001-R2");
        assertNotNull(chainByNumber);
        assertEquals("YT-QT-2026-000001", chainByNumber.getRootQuotationNo());
        assertEquals(2, chainByNumber.getTotalRevisions());
    }
}
