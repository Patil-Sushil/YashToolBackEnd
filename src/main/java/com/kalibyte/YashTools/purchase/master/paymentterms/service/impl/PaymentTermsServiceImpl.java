package com.kalibyte.YashTools.purchase.master.paymentterms.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.purchase.master.paymentterms.dto.request.PaymentTermsRequest;
import com.kalibyte.YashTools.purchase.master.paymentterms.dto.response.PaymentTermsResponse;
import com.kalibyte.YashTools.purchase.master.paymentterms.entity.PaymentTerms;
import com.kalibyte.YashTools.purchase.master.paymentterms.mapper.PaymentTermsMapper;
import com.kalibyte.YashTools.purchase.master.paymentterms.repository.PaymentTermsRepository;
import com.kalibyte.YashTools.purchase.master.paymentterms.service.PaymentTermsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentTermsServiceImpl implements PaymentTermsService {

    private final PaymentTermsRepository repository;
    private final PaymentTermsMapper mapper;

    @Override
    @Transactional
    @LoggableAction(value = "Create Payment Terms", action = AuditAction.PAYMENT_TERMS_CREATED, entityType = "PAYMENT_TERMS")
    public PaymentTermsResponse create(PaymentTermsRequest request) {
        log.info("Creating payment terms with code: {}", request.getCode());
        repository.findByCode(request.getCode().trim().toUpperCase()).ifPresent(pt -> {
            throw new BusinessException("Payment terms with code " + request.getCode() + " already exists.");
        });

        PaymentTerms entity = mapper.toEntity(request);
        entity.setCode(request.getCode().trim().toUpperCase());
        if (request.getActive() == null) {
            entity.setActive(true);
        }
        PaymentTerms saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public PaymentTermsResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terms not found with ID: " + id));
    }

    @Override
    public List<PaymentTermsResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @LoggableAction(value = "Update Payment Terms", action = AuditAction.PAYMENT_TERMS_UPDATED, entityType = "PAYMENT_TERMS")
    public PaymentTermsResponse update(UUID id, PaymentTermsRequest request) {
        log.info("Updating payment terms with ID: {}", id);
        PaymentTerms entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terms not found with ID: " + id));

        repository.findByCode(request.getCode().trim().toUpperCase()).ifPresent(pt -> {
            if (!pt.getId().equals(id)) {
                throw new BusinessException("Payment terms with code " + request.getCode() + " already exists.");
            }
        });

        mapper.updateEntityFromRequest(request, entity);
        entity.setCode(request.getCode().trim().toUpperCase());
        PaymentTerms saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @LoggableAction(value = "Delete Payment Terms", action = AuditAction.PAYMENT_TERMS_DELETED, entityType = "PAYMENT_TERMS")
    public void delete(UUID id) {
        log.info("Deleting payment terms with ID: {}", id);
        PaymentTerms entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terms not found with ID: " + id));
        repository.delete(entity);
    }
}
