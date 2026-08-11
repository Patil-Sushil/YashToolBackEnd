package com.kalibyte.YashTools.production.finishedgoods.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.finishedgoods.dto.FinishedGoodsStockResponse;
import com.kalibyte.YashTools.production.finishedgoods.entity.FinishedGoodsStock;
import com.kalibyte.YashTools.production.finishedgoods.repository.FinishedGoodsStockRepository;
import com.kalibyte.YashTools.production.finishedgoods.service.FinishedGoodsStockService;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinishedGoodsStockServiceImpl implements FinishedGoodsStockService {

    private final FinishedGoodsStockRepository repository;

    @Override
    @Transactional
    public void addFinishedGoodsStock(WorkOrderItem workOrderItem, Integer quantity) {
        UUID companyId = CompanyContextHolder.getCompanyId();

        FinishedGoodsStock stock = repository.findStock(workOrderItem.getId(), companyId)
                .orElseGet(() -> {
                    FinishedGoodsStock s = FinishedGoodsStock.builder()
                            .workOrderItem(workOrderItem)
                            .quantity(0)
                            .build();
                    s.setCompany(workOrderItem.getWorkOrder().getCompany());
                    return s;
                });

        stock.setQuantity(stock.getQuantity() + quantity);
        repository.save(stock);
        log.info("Incremented Finished Goods Stock for work order item {}. New Quantity: {}", workOrderItem.getId(), stock.getQuantity());
    }

    @Override
    @Transactional
    public void deductFinishedGoodsStock(WorkOrderItem workOrderItem, Integer quantity) {
        UUID companyId = CompanyContextHolder.getCompanyId();

        FinishedGoodsStock stock = repository.findStock(workOrderItem.getId(), companyId)
                .orElseThrow(() -> new BusinessException("No finished goods stock found for work order item: " + workOrderItem.getId()));

        if (stock.getQuantity() < quantity) {
            throw new BusinessException("Insufficient finished goods stock. Available: " + stock.getQuantity() + ", Required: " + quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        repository.save(stock);
        log.info("Decremented Finished Goods Stock for work order item {}. New Quantity: {}", workOrderItem.getId(), stock.getQuantity());
    }

    @Override
    @Transactional(readOnly = true)
    public FinishedGoodsStockResponse getStockByWorkOrderItem(UUID workOrderItemId) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        FinishedGoodsStock stock = repository.findStock(workOrderItemId, companyId)
                .orElseThrow(() -> new BusinessException("Finished goods stock record not found for work order item: " + workOrderItemId));
        return toResponse(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinishedGoodsStockResponse> getAllFinishedGoodsStock(int page, int size) {
        Page<FinishedGoodsStock> paged = repository.findAll(PageRequest.of(page, size));
        return PageResponse.from(paged, this::toResponse);
    }

    private FinishedGoodsStockResponse toResponse(FinishedGoodsStock stock) {
        WorkOrderItem item = stock.getWorkOrderItem();
        return FinishedGoodsStockResponse.builder()
                .id(stock.getId())
                .workOrderItemId(item.getId())
                .workOrderNo(item.getWorkOrder().getWorkOrderNo())
                .toolName(item.getToolName())
                .itemName(item.getItemName())
                .quantity(stock.getQuantity())
                .diameter(item.getDiameter())
                .shankDiameter(item.getShankDiameter())
                .overallLength(item.getOverallLength())
                .fluteLength(item.getFluteLength())
                .drawingReference(item.getDrawingReference())
                .materialGrade(item.getMaterialGrade() != null ? item.getMaterialGrade().name() : null)
                .materialType(item.getMaterialType())
                .coatingType(item.getCoatingType())
                .technicalNotes(item.getTechnicalNotes())
                .companyId(stock.getCompany().getId())
                .build();
    }
}
