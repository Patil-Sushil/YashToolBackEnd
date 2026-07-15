package com.kalibyte.YashTools.purchase;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import com.kalibyte.YashTools.purchase.master.vendor.repository.VendorRepository;
import com.kalibyte.YashTools.purchase.shared.enums.PaymentMethod;
import com.kalibyte.YashTools.purchase.shared.enums.PaymentStatus;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.mapper.PurchaseInvoiceMapper;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request.VendorPaymentItemRequest;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request.VendorPaymentRequest;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response.VendorPaymentResponse;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPayment;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPaymentItem;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.mapper.VendorPaymentMapper;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.repository.VendorPaymentItemRepository;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.repository.VendorPaymentRepository;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.service.impl.VendorPaymentServiceImpl;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.validator.VendorPaymentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendorPaymentServiceImplTest {

    @Mock
    private VendorPaymentRepository repository;
    @Mock
    private VendorPaymentItemRepository vendorPaymentItemRepository;
    @Mock
    private PurchaseInvoiceRepository purchaseInvoiceRepository;
    @Mock
    private VendorRepository vendorRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private VendorPaymentMapper mapper;
    @Mock
    private PurchaseInvoiceMapper purchaseInvoiceMapper;
    @Mock
    private VendorPaymentValidator validator;

    @InjectMocks
    private VendorPaymentServiceImpl service;

    private Company company;
    private Vendor vendor;
    private PurchaseInvoice invoice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        company = new Company();
        company.setId(UUID.randomUUID());
        company.setCode("YT");

        vendor = new Vendor();
        vendor.setId(UUID.randomUUID());
        vendor.setVendorName("ABC Carbide");

        invoice = new PurchaseInvoice();
        invoice.setId(UUID.randomUUID());
        invoice.setInvoiceNumber("PI-001");
        invoice.setTotalAmount(new BigDecimal("100000.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setOutstandingAmount(new BigDecimal("100000.00"));
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        invoice.setStatus(PurchaseInvoiceStatus.APPROVED);
        invoice.setCompany(company);
        invoice.setVendor(vendor);

        com.kalibyte.YashTools.common.multi_company.CompanyContextHolder.setCompanyId(company.getId());

        when(companyRepository.findById(any(UUID.class))).thenReturn(Optional.of(company));
        when(vendorRepository.findById(any(UUID.class))).thenReturn(Optional.of(vendor));
        when(purchaseInvoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
    }

    @Test
    void create_Success_FullPayment() {
        VendorPaymentRequest request = new VendorPaymentRequest();
        request.setVendorId(vendor.getId());
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setPaymentReferenceNumber("TXN-1234");
        request.setPaymentDate(LocalDate.now());

        VendorPaymentItemRequest itemReq = new VendorPaymentItemRequest(invoice.getId(), new BigDecimal("100000.00"));
        request.setItems(Collections.singletonList(itemReq));

        VendorPayment payment = new VendorPayment();
        payment.setPaymentNumber("YT-PAY-2026-00001");
        payment.setCompany(company);
        payment.setVendor(vendor);

        VendorPaymentItem paymentItem = new VendorPaymentItem();
        paymentItem.setPurchaseInvoice(invoice);
        paymentItem.setPaidAmount(new BigDecimal("100000.00"));
        payment.addItem(paymentItem);

        when(mapper.toEntity(any())).thenReturn(payment);
        when(mapper.toItemEntity(any())).thenReturn(paymentItem);
        when(repository.save(any())).thenReturn(payment);

        VendorPaymentResponse responseDto = new VendorPaymentResponse();
        responseDto.setPaymentNumber("YT-PAY-2026-00001");
        when(mapper.toResponse(any())).thenReturn(responseDto);

        VendorPaymentResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(0, new BigDecimal("100000.00").compareTo(response.getTotalPayment()));
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.getOutstandingAmount()));
        assertEquals(0, new BigDecimal("100000.00").compareTo(invoice.getPaidAmount()));

        verify(repository, times(1)).save(any());
        verify(purchaseInvoiceRepository, times(1)).save(invoice);
    }

    @Test
    void create_Success_PartialPayment() {
        VendorPaymentRequest request = new VendorPaymentRequest();
        request.setVendorId(vendor.getId());
        request.setPaymentMethod(PaymentMethod.UPI);
        request.setPaymentDate(LocalDate.now());

        VendorPaymentItemRequest itemReq = new VendorPaymentItemRequest(invoice.getId(), new BigDecimal("40000.00"));
        request.setItems(Collections.singletonList(itemReq));

        VendorPayment payment = new VendorPayment();
        payment.setPaymentNumber("YT-PAY-2026-00002");
        payment.setCompany(company);
        payment.setVendor(vendor);

        VendorPaymentItem paymentItem = new VendorPaymentItem();
        paymentItem.setPurchaseInvoice(invoice);
        paymentItem.setPaidAmount(new BigDecimal("40000.00"));
        payment.addItem(paymentItem);

        when(mapper.toEntity(any())).thenReturn(payment);
        when(mapper.toItemEntity(any())).thenReturn(paymentItem);
        when(repository.save(any())).thenReturn(payment);

        VendorPaymentResponse responseDto = new VendorPaymentResponse();
        responseDto.setPaymentNumber("YT-PAY-2026-00002");
        when(mapper.toResponse(any())).thenReturn(responseDto);

        VendorPaymentResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(0, new BigDecimal("40000.00").compareTo(response.getTotalPayment()));
        assertEquals(PaymentStatus.PARTIALLY_PAID, invoice.getPaymentStatus());
        assertEquals(0, new BigDecimal("60000.00").compareTo(invoice.getOutstandingAmount()));
        assertEquals(0, new BigDecimal("40000.00").compareTo(invoice.getPaidAmount()));

        verify(repository, times(1)).save(any());
        verify(purchaseInvoiceRepository, times(1)).save(invoice);
    }

    @Test
    void delete_Success() {
        VendorPayment payment = new VendorPayment();
        payment.setId(UUID.randomUUID());
        payment.setPaymentNumber("YT-PAY-2026-00001");
        payment.setCompany(company);
        payment.setVendor(vendor);

        VendorPaymentItem item = new VendorPaymentItem();
        item.setPurchaseInvoice(invoice);
        item.setPaidAmount(new BigDecimal("40000.00"));
        payment.addItem(item);

        invoice.setPaidAmount(new BigDecimal("40000.00"));
        invoice.setOutstandingAmount(new BigDecimal("60000.00"));
        invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);

        when(repository.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.delete(payment.getId());

        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.getPaidAmount()));
        assertEquals(0, new BigDecimal("100000.00").compareTo(invoice.getOutstandingAmount()));
        assertEquals(PaymentStatus.PENDING, invoice.getPaymentStatus());

        verify(repository, times(1)).delete(payment);
        verify(purchaseInvoiceRepository, times(1)).save(invoice);
    }
}
