package com.kalibyte.YashTools.production.logistics.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanItemRequest;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanRequest;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryReceiptRequest;
import com.kalibyte.YashTools.production.logistics.entity.DeliveryChallan;
import com.kalibyte.YashTools.production.logistics.entity.DeliveryChallanItem;
import com.kalibyte.YashTools.production.logistics.repository.DeliveryChallanRepository;
import com.kalibyte.YashTools.production.logistics.service.DeliveryChallanService;
import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import com.kalibyte.YashTools.workorder.repository.WorkOrderItemRepository;
import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import com.kalibyte.YashTools.production.logistics.repository.DeliveryChallanItemRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryChallanServiceImpl implements DeliveryChallanService {

    private final DeliveryChallanRepository repository;
    private final DeliveryChallanItemRepository deliveryChallanItemRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderItemRepository workOrderItemRepository;

    @Override
    @Transactional
    public DeliveryChallanResponse createChallan(DeliveryChallanRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        WorkOrder wo = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new BusinessException("Work Order not found with ID: " + request.getWorkOrderId()));

        if (!wo.getCompany().getId().equals(companyId)) {
            throw new BusinessException("Work Order does not belong to the active company context");
        }

        long count = repository.count() + 1;
        String challanNo = String.format("%s-DC-%d-%06d", companyCode, LocalDate.now().getYear(), count);

        DeliveryChallan dc = DeliveryChallan.builder()
                .challanNo(challanNo)
                .workOrder(wo)
                .vehicleNo(request.getVehicleNo())
                .driverName(request.getDriverName())
                .driverContact(request.getDriverContact())
                .status("DRAFT")
                .remarks(request.getRemarks())
                .items(new ArrayList<>())
                .build();
        dc.setCompany(wo.getCompany());

        for (DeliveryChallanItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new BusinessException("Delivery Challan item quantity must be greater than zero");
            }

            WorkOrderItem woi = workOrderItemRepository.findById(itemReq.getWorkOrderItemId())
                    .orElseThrow(() -> new BusinessException("Work Order Item not found with ID: " + itemReq.getWorkOrderItemId()));

            if (!woi.getWorkOrder().getId().equals(wo.getId())) {
                throw new BusinessException("Item " + woi.getId() + " does not belong to Work Order " + wo.getId());
            }

            int alreadyChallanedQty = deliveryChallanItemRepository.getSumQuantityByWorkOrderItemId(woi.getId());
            int totalItemQty = woi.getQuantity() != null ? woi.getQuantity() : 0;
            int remainingQty = totalItemQty - alreadyChallanedQty;

            if (itemReq.getQuantity() > remainingQty) {
                String itemName = (woi.getToolName() != null && !woi.getToolName().isBlank()) ? woi.getToolName() : woi.getItemName();
                throw new BusinessException(String.format("Requested quantity (%d) for item '%s' exceeds remaining un-challaned quantity (%d). Total ordered: %d, Already challaned: %d",
                        itemReq.getQuantity(), itemName, remainingQty, totalItemQty, alreadyChallanedQty));
            }

            DeliveryChallanItem dci = DeliveryChallanItem.builder()
                    .workOrderItem(woi)
                    .quantity(itemReq.getQuantity())
                    .build();
            dc.addItem(dci);
        }

        DeliveryChallan saved = repository.save(dc);
        log.info("Created Delivery Challan {} for Work Order {}", challanNo, wo.getWorkOrderNo());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryChallanResponse getChallanById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        DeliveryChallan dc = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Delivery Challan not found with ID: " + id));
        return toResponse(dc);
    }

    @Override
    @Transactional
    public DeliveryChallanResponse dispatchChallan(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        DeliveryChallan dc = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Delivery Challan not found with ID: " + id));

        if (!"DRAFT".equals(dc.getStatus())) {
            throw new BusinessException("Only DRAFT challans can be dispatched");
        }

        dc.setStatus("DISPATCHED");
        dc.setDeliveryDate(LocalDate.now());
        DeliveryChallan saved = repository.save(dc);

        log.info("Dispatched Delivery Challan {}", dc.getChallanNo());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryChallanResponse recordDeliveryReceipt(UUID id, DeliveryReceiptRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        DeliveryChallan dc = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Delivery Challan not found with ID: " + id));

        if (!"DISPATCHED".equals(dc.getStatus())) {
            throw new BusinessException("Delivery receipt can only be recorded for DISPATCHED challans");
        }

        dc.setStatus("DELIVERED");
        dc.setDeliveryReceiptBy(request.getReceivedBy());
        dc.setDeliveryReceiptAt(LocalDateTime.now());
        DeliveryChallan saved = repository.save(dc);

        // Update Work Order status to COMPLETED if fully delivered
        WorkOrder wo = dc.getWorkOrder();
        boolean allItemsDelivered = wo.getItems().stream().allMatch(item -> {
            int deliveredQty = deliveryChallanItemRepository.getSumDeliveredQuantityByWorkOrderItemId(item.getId());
            return deliveredQty >= item.getQuantity();
        });

        if (allItemsDelivered) {
            wo.setStatus(WorkOrderStatus.COMPLETED);
            workOrderRepository.save(wo);
        } else {
            if (wo.getStatus() == WorkOrderStatus.CREATED) {
                wo.setStatus(WorkOrderStatus.IN_PROGRESS);
                workOrderRepository.save(wo);
            }
        }

        log.info("Recorded Delivery Receipt for Challan {} by {}", dc.getChallanNo(), request.getReceivedBy());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeliveryChallanResponse> getAllChallans(int page, int size) {
        Page<DeliveryChallan> paged = repository.findAll(PageRequest.of(page, size));
        return PageResponse.from(paged, this::toResponse);
    }

    private DeliveryChallanResponse toResponse(DeliveryChallan dc) {
        return DeliveryChallanResponse.builder()
                .id(dc.getId())
                .challanNo(dc.getChallanNo())
                .workOrderId(dc.getWorkOrder().getId())
                .workOrderNo(dc.getWorkOrder().getWorkOrderNo())
                .customerName(dc.getWorkOrder().getCustomerCompanyName())
                .vehicleNo(dc.getVehicleNo())
                .driverName(dc.getDriverName())
                .driverContact(dc.getDriverContact())
                .status(dc.getStatus())
                .deliveryDate(dc.getDeliveryDate())
                .deliveryReceiptBy(dc.getDeliveryReceiptBy())
                .deliveryReceiptAt(dc.getDeliveryReceiptAt())
                .remarks(dc.getRemarks())
                .companyId(dc.getCompany() != null ? dc.getCompany().getId() : null)
                .companyCode(dc.getCompany() != null ? dc.getCompany().getCode() : null)
                .customerEmail(dc.getWorkOrder() != null ? dc.getWorkOrder().getCustomerEmail() : null)
                .items(dc.getItems().stream()
                        .map(i -> DeliveryChallanResponse.ItemResponse.builder()
                                .id(i.getId())
                                .workOrderItemId(i.getWorkOrderItem().getId())
                                .toolName(i.getWorkOrderItem().getToolName())
                                .itemName(i.getWorkOrderItem().getItemName())
                                .quantity(i.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
