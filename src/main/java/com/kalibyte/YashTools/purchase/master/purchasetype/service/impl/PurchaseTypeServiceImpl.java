package com.kalibyte.YashTools.purchase.master.purchasetype.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.purchase.master.purchasetype.dto.request.PurchaseTypeRequest;
import com.kalibyte.YashTools.purchase.master.purchasetype.dto.response.PurchaseTypeResponse;
import com.kalibyte.YashTools.purchase.master.purchasetype.entity.PurchaseType;
import com.kalibyte.YashTools.purchase.master.purchasetype.mapper.PurchaseTypeMapper;
import com.kalibyte.YashTools.purchase.master.purchasetype.repository.PurchaseTypeRepository;
import com.kalibyte.YashTools.purchase.master.purchasetype.service.PurchaseTypeService;
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
public class PurchaseTypeServiceImpl implements PurchaseTypeService {

    private final PurchaseTypeRepository repository;
    private final PurchaseTypeMapper mapper;

    @Override
    @Transactional
    @LoggableAction(value = "Create Purchase Type", action = AuditAction.PURCHASE_TYPE_CREATED, entityType = "PURCHASE_TYPE")
    public PurchaseTypeResponse create(PurchaseTypeRequest request) {
        log.info("Creating purchase type with code: {}", request.getCode());
        repository.findByCode(request.getCode().trim().toUpperCase()).ifPresent(pt -> {
            throw new BusinessException("Purchase type with code " + request.getCode() + " already exists.");
        });

        PurchaseType entity = mapper.toEntity(request);
        entity.setCode(request.getCode().trim().toUpperCase());
        if (request.getActive() == null) {
            entity.setActive(true);
        }
        PurchaseType saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public PurchaseTypeResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase type not found with ID: " + id));
    }

    @Override
    public List<PurchaseTypeResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @LoggableAction(value = "Update Purchase Type", action = AuditAction.PURCHASE_TYPE_UPDATED, entityType = "PURCHASE_TYPE")
    public PurchaseTypeResponse update(UUID id, PurchaseTypeRequest request) {
        log.info("Updating purchase type with ID: {}", id);
        PurchaseType entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase type not found with ID: " + id));

        repository.findByCode(request.getCode().trim().toUpperCase()).ifPresent(pt -> {
            if (!pt.getId().equals(id)) {
                throw new BusinessException("Purchase type with code " + request.getCode() + " already exists.");
            }
        });

        mapper.updateEntityFromRequest(request, entity);
        entity.setCode(request.getCode().trim().toUpperCase());
        PurchaseType saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @LoggableAction(value = "Delete Purchase Type", action = AuditAction.PURCHASE_TYPE_DELETED, entityType = "PURCHASE_TYPE")
    public void delete(UUID id) {
        log.info("Deleting purchase type with ID: {}", id);
        PurchaseType entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase type not found with ID: " + id));
        repository.delete(entity);
    }
}
