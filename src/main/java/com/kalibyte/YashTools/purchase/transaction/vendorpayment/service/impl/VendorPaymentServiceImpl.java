package com.kalibyte.YashTools.purchase.transaction.vendorpayment.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import com.kalibyte.YashTools.purchase.master.vendor.repository.VendorRepository;
import com.kalibyte.YashTools.purchase.shared.enums.PaymentStatus;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.mapper.PurchaseInvoiceMapper;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request.VendorPaymentRequest;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response.VendorPaymentResponse;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response.VendorPaymentItemResponse;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPayment;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPaymentItem;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.mapper.VendorPaymentMapper;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.repository.VendorPaymentItemRepository;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.repository.VendorPaymentRepository;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.service.VendorPaymentService;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.validator.VendorPaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorPaymentServiceImpl implements VendorPaymentService {

    private final VendorPaymentRepository repository;
    private final VendorPaymentItemRepository vendorPaymentItemRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final VendorRepository vendorRepository;
    private final CompanyRepository companyRepository;
    private final VendorPaymentMapper mapper;
    private final PurchaseInvoiceMapper purchaseInvoiceMapper;
    private final VendorPaymentValidator validator;

    @Override
    @Transactional
    @LoggableAction(value = "Create Vendor Payment", action = AuditAction.VENDOR_PAYMENT_CREATED, entityType = "VENDOR_PAYMENT")
    public VendorPaymentResponse create(VendorPaymentRequest request) {
        log.info("Creating Vendor Payment for vendor: {}", request.getVendorId());

        UUID companyId = com.kalibyte.YashTools.common.multi_company.CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            var defaultComp = companyRepository.findByCode("YT")
                    .orElseThrow(() -> new IllegalStateException("Default company (YT) not found."));
            companyId = defaultComp.getId();
        }
        final UUID finalCompanyId = companyId;
        var company = companyRepository.findById(finalCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + finalCompanyId));

        validator.validateCreate(finalCompanyId, request);

        var vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getVendorId()));

        VendorPayment payment = mapper.toEntity(request);
        payment.setVendor(vendor);
        payment.setCompany(company);

        if (request.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDate.now());
        }

        payment.setPaymentNumber(generatePaymentNumber(company.getCode()));
        payment.setItems(new ArrayList<>());

        BigDecimal totalPayment = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            var invoice = purchaseInvoiceRepository.findById(itemReq.getPurchaseInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found with ID: " + itemReq.getPurchaseInvoiceId()));

            validator.validateInvoiceForPayment(invoice, itemReq.getPaidAmount());

            BigDecimal outstandingBefore = invoice.getOutstandingAmount();
            BigDecimal outstandingAfter = outstandingBefore.subtract(itemReq.getPaidAmount());

            VendorPaymentItem item = mapper.toItemEntity(itemReq);
            item.setPurchaseInvoice(invoice);
            item.setInvoiceNumber(invoice.getInvoiceNumber());
            item.setInvoiceAmount(invoice.getTotalAmount());
            item.setOutstandingBeforePayment(outstandingBefore);
            item.setOutstandingAfterPayment(outstandingAfter);

            payment.addItem(item);

            invoice.setPaidAmount(invoice.getPaidAmount().add(itemReq.getPaidAmount()));
            invoice.setOutstandingAmount(outstandingAfter);

            if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                invoice.setPaymentStatus(PaymentStatus.PAID);
            } else {
                invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
            }

            purchaseInvoiceRepository.save(invoice);
            totalPayment = totalPayment.add(itemReq.getPaidAmount());
        }

        VendorPayment saved = repository.save(payment);
        var response = mapper.toResponse(saved);
        response.setTotalPayment(totalPayment);
        return response;
    }

    @Override
    public VendorPaymentResponse getById(UUID id) {
        VendorPayment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor Payment not found with ID: " + id));
        var response = mapper.toResponse(payment);
        enrichResponse(payment, response);
        return response;
    }

    @Override
    public List<VendorPaymentResponse> getAll() {
        return repository.findAll().stream()
                .map(payment -> {
                    var response = mapper.toResponse(payment);
                    enrichResponse(payment, response);
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    @LoggableAction(value = "Update Vendor Payment", action = AuditAction.VENDOR_PAYMENT_UPDATED, entityType = "VENDOR_PAYMENT")
    public VendorPaymentResponse update(UUID id, VendorPaymentRequest request) {
        log.info("Updating Vendor Payment: {}", id);

        VendorPayment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor Payment not found with ID: " + id));

        UUID companyId = payment.getCompany().getId();
        validator.validateUpdate(companyId, request, id);

        // 1. Revert previous payment allocations
        for (var item : payment.getItems()) {
            var invoice = item.getPurchaseInvoice();
            invoice.setPaidAmount(invoice.getPaidAmount().subtract(item.getPaidAmount()));
            invoice.setOutstandingAmount(invoice.getOutstandingAmount().add(item.getPaidAmount()));

            if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
                invoice.setPaymentStatus(PaymentStatus.PENDING);
            } else {
                invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
            }
            purchaseInvoiceRepository.save(invoice);
        }

        purchaseInvoiceRepository.flush();

        // 2. Update header details
        var vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getVendorId()));
        payment.setVendor(vendor);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentReferenceNumber(request.getPaymentReferenceNumber());
        payment.setRemarks(request.getRemarks());
        if (request.getPaymentDate() != null) {
            payment.setPaymentDate(request.getPaymentDate());
        }

        // 3. Clear existing items and delete from DB first to prevent state conflicts
        var itemsToRemove = new ArrayList<>(payment.getItems());
        payment.getItems().clear();
        vendorPaymentItemRepository.deleteAll(itemsToRemove);
        vendorPaymentItemRepository.flush();

        BigDecimal totalPayment = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            var invoice = purchaseInvoiceRepository.findById(itemReq.getPurchaseInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found with ID: " + itemReq.getPurchaseInvoiceId()));

            validator.validateInvoiceForPayment(invoice, itemReq.getPaidAmount());

            BigDecimal outstandingBefore = invoice.getOutstandingAmount();
            BigDecimal outstandingAfter = outstandingBefore.subtract(itemReq.getPaidAmount());

            VendorPaymentItem item = mapper.toItemEntity(itemReq);
            item.setPurchaseInvoice(invoice);
            item.setInvoiceNumber(invoice.getInvoiceNumber());
            item.setInvoiceAmount(invoice.getTotalAmount());
            item.setOutstandingBeforePayment(outstandingBefore);
            item.setOutstandingAfterPayment(outstandingAfter);

            payment.addItem(item);

            invoice.setPaidAmount(invoice.getPaidAmount().add(itemReq.getPaidAmount()));
            invoice.setOutstandingAmount(outstandingAfter);

            if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                invoice.setPaymentStatus(PaymentStatus.PAID);
            } else {
                invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
            }

            purchaseInvoiceRepository.save(invoice);
            totalPayment = totalPayment.add(itemReq.getPaidAmount());
        }

        VendorPayment saved = repository.save(payment);
        var response = mapper.toResponse(saved);
        response.setTotalPayment(totalPayment);
        return response;
    }

    @Override
    @Transactional
    @LoggableAction(value = "Delete Vendor Payment", action = AuditAction.VENDOR_PAYMENT_DELETED, entityType = "VENDOR_PAYMENT")
    public void delete(UUID id) {
        log.info("Deleting Vendor Payment: {}", id);

        VendorPayment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor Payment not found with ID: " + id));

        // Revert all payment allocations
        for (var item : payment.getItems()) {
            var invoice = item.getPurchaseInvoice();
            invoice.setPaidAmount(invoice.getPaidAmount().subtract(item.getPaidAmount()));
            invoice.setOutstandingAmount(invoice.getOutstandingAmount().add(item.getPaidAmount()));

            if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
                invoice.setPaymentStatus(PaymentStatus.PENDING);
            } else {
                invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
            }
            purchaseInvoiceRepository.save(invoice);
        }

        repository.delete(payment);
    }

    @Override
    public List<PurchaseInvoiceResponse> getOutstandingReport(UUID vendorId) {
        log.info("Generating vendor outstanding report. Vendor ID: {}", vendorId);
        List<PurchaseInvoice> invoices;
        if (vendorId != null) {
            invoices = purchaseInvoiceRepository.findByVendorIdAndStatusAndOutstandingAmountGreaterThan(
                    vendorId, PurchaseInvoiceStatus.APPROVED, BigDecimal.ZERO);
        } else {
            invoices = purchaseInvoiceRepository.findByStatusAndOutstandingAmountGreaterThan(
                    PurchaseInvoiceStatus.APPROVED, BigDecimal.ZERO);
        }
        return invoices.stream()
                .map(this::mapInvoiceToResponse)
                .toList();
    }

    @Override
    public List<VendorPaymentResponse> getPaymentHistoryReport(UUID vendorId) {
        log.info("Generating vendor payment history report. Vendor ID: {}", vendorId);
        List<VendorPayment> payments;
        if (vendorId != null) {
            payments = repository.findByVendorId(vendorId);
        } else {
            payments = repository.findAll();
        }
        return payments.stream()
                .map(payment -> {
                    var response = mapper.toResponse(payment);
                    enrichResponse(payment, response);
                    return response;
                })
                .toList();
    }

    @Override
    public List<PurchaseInvoiceResponse> getOverdueInvoices() {
        log.info("Generating overdue invoices report.");
        List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findByStatusAndOutstandingAmountGreaterThanAndDueDateLessThan(
                PurchaseInvoiceStatus.APPROVED, BigDecimal.ZERO, LocalDate.now());
        return invoices.stream()
                .map(this::mapInvoiceToResponse)
                .toList();
    }

    private void enrichResponse(VendorPayment entity, VendorPaymentResponse response) {
        BigDecimal total = entity.getItems().stream()
                .map(VendorPaymentItem::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalPayment(total);
    }

    private PurchaseInvoiceResponse mapInvoiceToResponse(PurchaseInvoice invoice) {
        PurchaseInvoiceResponse response = purchaseInvoiceMapper.toResponse(invoice);
        if (invoice.getGoodsReceipts() != null) {
            response.setGoodsReceiptIds(invoice.getGoodsReceipts().stream().map(grn -> grn.getId()).toList());
            response.setGoodsReceiptNumbers(invoice.getGoodsReceipts().stream().map(grn -> grn.getGrnNumber()).toList());
        }
        return response;
    }

    private String generatePaymentNumber(String companyCode) {
        int year = LocalDate.now().getYear();
        Optional<VendorPayment> lastPayment = repository.findTopByOrderByCreatedAtDesc();
        long nextNumber = 1;

        if (lastPayment.isPresent()) {
            String lastNumber = lastPayment.get().getPaymentNumber();
            String[] parts = lastNumber.split("-");
            if (parts.length >= 4) {
                try {
                    nextNumber = Long.parseLong(parts[parts.length - 1]) + 1;
                } catch (NumberFormatException e) {
                    // Keep 1
                }
            }
        }

        String companyPrefix = companyCode != null && !companyCode.trim().isEmpty() ? companyCode.trim() : "YT";
        return String.format("%s-PAY-%d-%05d", companyPrefix, year, nextNumber);
    }
}
