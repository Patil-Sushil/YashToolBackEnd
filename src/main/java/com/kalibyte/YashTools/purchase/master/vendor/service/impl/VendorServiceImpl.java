package com.kalibyte.YashTools.purchase.master.vendor.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.purchase.master.paymentterms.entity.PaymentTerms;
import com.kalibyte.YashTools.purchase.master.paymentterms.repository.PaymentTermsRepository;
import com.kalibyte.YashTools.purchase.master.vendor.dto.request.VendorRequest;
import com.kalibyte.YashTools.purchase.master.vendor.dto.response.VendorResponse;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import com.kalibyte.YashTools.purchase.master.vendor.mapper.VendorMapper;
import com.kalibyte.YashTools.purchase.master.vendor.repository.VendorRepository;
import com.kalibyte.YashTools.purchase.master.vendor.service.VendorService;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorServiceImpl implements VendorService {

    private final VendorRepository repository;
    private final PaymentTermsRepository paymentTermsRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final VendorMapper mapper;

    @Override
    @Transactional
    @LoggableAction(value = "Create Vendor", action = AuditAction.VENDOR_CREATED, entityType = "VENDOR")
    public VendorResponse create(VendorRequest request) {
        log.info("Creating vendor with name: {}", request.getVendorName());
        repository.findByVendorName(request.getVendorName().trim()).ifPresent(v -> {
            throw new BusinessException("Vendor with name '" + request.getVendorName() + "' already exists.");
        });

        Vendor entity = mapper.toEntity(request);
        entity.setVendorName(request.getVendorName().trim());

        if (request.getPaymentTermsId() != null) {
            PaymentTerms pt = paymentTermsRepository.findById(request.getPaymentTermsId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment Terms not found with ID: " + request.getPaymentTermsId()));
            entity.setPaymentTerms(pt);
        }

        if (request.getActive() == null) {
            entity.setActive(true);
        }

        Vendor saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public VendorResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + id));
    }

    @Override
    public List<VendorResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @LoggableAction(value = "Update Vendor", action = AuditAction.VENDOR_UPDATED, entityType = "VENDOR")
    public VendorResponse update(UUID id, VendorRequest request) {
        log.info("Updating vendor with ID: {}", id);
        Vendor entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + id));

        repository.findByVendorName(request.getVendorName().trim()).ifPresent(v -> {
            if (!v.getId().equals(id)) {
                throw new BusinessException("Vendor with name '" + request.getVendorName() + "' already exists.");
            }
        });

        mapper.updateEntityFromRequest(request, entity);
        entity.setVendorName(request.getVendorName().trim());

        if (request.getPaymentTermsId() != null) {
            PaymentTerms pt = paymentTermsRepository.findById(request.getPaymentTermsId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment Terms not found with ID: " + request.getPaymentTermsId()));
            entity.setPaymentTerms(pt);
        } else {
            entity.setPaymentTerms(null);
        }

        Vendor saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @LoggableAction(value = "Delete Vendor", action = AuditAction.VENDOR_DELETED, entityType = "VENDOR")
    public void delete(UUID id) {
        log.info("Deleting vendor with ID: {}", id);
        Vendor entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + id));
        repository.delete(entity);
    }

    @Override
    public BigDecimal getOutstandingBalance(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Vendor not found with ID: " + id);
        }
        List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findByVendorIdAndStatusAndOutstandingAmountGreaterThan(
                id, com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus.APPROVED, BigDecimal.ZERO);
        return invoices.stream()
                .map(PurchaseInvoice::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    public List<VendorResponse> searchVendors(String query) {
        List<Vendor> result = repository.searchVendors(query);
        return result.stream().map(mapper::toResponse).collect(java.util.stream.Collectors.toList());
    }

}
