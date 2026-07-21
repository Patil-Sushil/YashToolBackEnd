package com.kalibyte.YashTools.production.packing.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.finishedgoods.service.FinishedGoodsStockService;
import com.kalibyte.YashTools.production.packing.dto.PackingRequest;
import com.kalibyte.YashTools.production.packing.dto.PackingResponse;
import com.kalibyte.YashTools.production.packing.entity.PackingLog;
import com.kalibyte.YashTools.production.packing.repository.PackingLogRepository;
import com.kalibyte.YashTools.production.packing.service.PackingService;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.workorder.repository.WorkOrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackingServiceImpl implements PackingService {

    private final PackingLogRepository repository;
    private final WorkOrderItemRepository workOrderItemRepository;
    private final FinishedGoodsStockService finishedGoodsStockService;

    @Override
    @Transactional
    public PackingResponse recordPacking(PackingRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        WorkOrderItem woi = workOrderItemRepository.findById(request.getWorkOrderItemId())
                .orElseThrow(() -> new BusinessException("Work Order Item not found with ID: " + request.getWorkOrderItemId()));

        if (!woi.getWorkOrder().getCompany().getId().equals(companyId)) {
            throw new BusinessException("Work Order Item does not belong to the active company context");
        }

        // Deduct quantity from finished goods stock
        finishedGoodsStockService.deductFinishedGoodsStock(woi, request.getQuantity());

        // Generate unique packing number
        long count = repository.count() + 1;
        String packingNo = String.format("%s-PKG-%d-%06d", companyCode, LocalDate.now().getYear(), count);

        PackingLog logEntry = PackingLog.builder()
                .workOrderItem(woi)
                .packingNo(packingNo)
                .batchNo(request.getBatchNo())
                .packageSize(request.getPackageSize())
                .quantity(request.getQuantity())
                .containerStatus(request.getContainerStatus())
                .remarks(request.getRemarks())
                .build();
        logEntry.setCompany(woi.getWorkOrder().getCompany());

        PackingLog saved = repository.save(logEntry);
        log.info("Recorded packing log {} for work order item {} of quantity {}", packingNo, woi.getId(), request.getQuantity());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PackingResponse getPackingById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        PackingLog logEntry = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Packing log not found with ID: " + id));
        return toResponse(logEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackingResponse> getPackingByWorkOrderItem(UUID workOrderItemId) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        return repository.findByWorkOrderItemIdAndCompanyId(workOrderItemId, companyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackingResponse> getAllPackingLogs(int page, int size) {
        Page<PackingLog> paged = repository.findAll(PageRequest.of(page, size));
        return PageResponse.from(paged, this::toResponse);
    }

    private PackingResponse toResponse(PackingLog p) {
        return PackingResponse.builder()
                .id(p.getId())
                .workOrderItemId(p.getWorkOrderItem().getId())
                .workOrderNo(p.getWorkOrderItem().getWorkOrder().getWorkOrderNo())
                .toolName(p.getWorkOrderItem().getToolName())
                .itemName(p.getWorkOrderItem().getItemName())
                .packingNo(p.getPackingNo())
                .batchNo(p.getBatchNo())
                .packageSize(p.getPackageSize())
                .quantity(p.getQuantity())
                .containerStatus(p.getContainerStatus())
                .remarks(p.getRemarks())
                .createdAt(p.getCreatedAt())
                .createdBy(p.getCreatedBy())
                .build();
    }
}
