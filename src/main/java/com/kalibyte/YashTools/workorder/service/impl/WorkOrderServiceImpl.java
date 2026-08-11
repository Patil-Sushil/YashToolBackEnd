package com.kalibyte.YashTools.workorder.service.impl;

import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.QuotationItem;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.workorder.dto.request.CreateWorkOrderRequest;
import com.kalibyte.YashTools.workorder.dto.request.UpdateWorkOrderStatusRequest;
import com.kalibyte.YashTools.workorder.dto.request.UpdateTrialResultRequest;
import com.kalibyte.YashTools.workorder.dto.request.UpdateWorkOrderPlanningRequest;
import com.kalibyte.YashTools.workorder.dto.response.WorkOrderItemResponse;
import com.kalibyte.YashTools.workorder.dto.response.WorkOrderResponse;
import com.kalibyte.YashTools.workorder.dto.response.LockedQuotationSummaryResponse;
import com.kalibyte.YashTools.workorder.dto.response.PendingDispatchWorkOrderResponse;
import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import com.kalibyte.YashTools.workorder.entity.enums.TrialStatus;
import com.kalibyte.YashTools.workorder.exception.WorkOrderException;
import com.kalibyte.YashTools.workorder.repository.WorkOrderItemRepository;
import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;
import com.kalibyte.YashTools.workorder.service.WorkOrderService;
import com.kalibyte.YashTools.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final QuotationRepository quotationRepository;
    private final QuotationSecurityService quotationSecurityService;
    private final WorkOrderItemRepository workOrderItemRepository;
    private final com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository jobCardRepository;
    private final com.kalibyte.YashTools.production.execution.repository.ExecutionLogRepository executionLogRepository;
    private final com.kalibyte.YashTools.production.tracking.repository.QualityInspectionRepository qualityInspectionRepository;
    private final com.kalibyte.YashTools.production.packing.repository.PackingLogRepository packingLogRepository;
    private final com.kalibyte.YashTools.production.logistics.repository.DeliveryChallanItemRepository deliveryChallanItemRepository;
    private final com.kalibyte.YashTools.sales.invoice.repository.SalesInvoiceItemRepository salesInvoiceItemRepository;


    @Override
    @Transactional
    public WorkOrderResponse createFromQuotation(CreateWorkOrderRequest request) {
        log.info("Converting quotation {} to Work Order", request.getQuotationId());

        Quotation quotation = quotationSecurityService.loadForCurrentCompany(request.getQuotationId());

        if (quotation.getStatus() != QuotationStatus.LOCKED) {
            throw new WorkOrderException("Only LOCKED quotations can be converted to a Work Order. Current status: " + quotation.getStatus());
        }

        if (workOrderRepository.existsByQuotationId(quotation.getId())) {
            throw new WorkOrderException("A Work Order already exists for Quotation: " + quotation.getQuotationNo());
        }

        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            companyCode = "YT";
        }

        long seq = workOrderRepository.count() + 1;
        String workOrderNo = String.format("%s-WO-%d-%06d", companyCode, LocalDate.now().getYear(), seq);

        Company company = Company.builder().id(companyId).code(companyCode).build();

        String shippingAddr = quotation.getCustomer() != null ? quotation.getCustomer().getDeliveryAddress() : null;

        WorkOrder wo = WorkOrder.builder()
                .workOrderNo(workOrderNo)
                .quotation(quotation)
                .status(WorkOrderStatus.CREATED)
                .customer(quotation.getCustomer())
                .customerCompanyName(quotation.getCustomerCompanyName())
                .customerContactPerson(quotation.getCustomerContactPerson())
                .customerEmail(quotation.getCustomerEmail())
                .customerMobile(quotation.getCustomerMobile())
                .shippingAddress(shippingAddr)
                .remarks(request.getRemarks())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .poNumber(request.getPoNumber())
                .build();
        wo.setCompany(company);

        int line = 1;
        for (QuotationItem src : quotation.getItems()) {
            WorkOrderItem target = WorkOrderItem.builder()
                    .lineNumber(line++)
                    .quotationItem(src)
                    .orderType(src.getOrderType())
                    .toolName(src.getToolName())
                    .itemName(src.getItemName())
                    .quantity(src.getQuantity())
                    .trial(src.getTrial())
                    .trialStatus(Boolean.TRUE.equals(src.getTrial()) ? TrialStatus.PENDING : null)
                    .itemRemarks(src.getItemRemarks())
                    .drawingReference(src.getDrawingReference())
                    .materialType(src.getMaterialType())
                    .materialGrade(src.getMaterialGrade())
                    .coatingRequired(src.getCoatingRequired())
                    .coatingType(src.getCoatingType())
                    .resharpeningType(src.getResharpeningType())
                    .diameter(src.getDiameter())
                    .fluteLength(src.getFluteLength())
                    .shankDiameter(src.getShankDiameter())
                    .overallLength(src.getOverallLength())
                    .technicalNotes(src.getTechnicalNotes())
                    .damageLevel(src.getDamageLevel())
                    .specialGeometry(src.getSpecialGeometry())
                    .specialProfile(src.getSpecialProfile())
                    .expressDelivery(src.getExpressDelivery())
                    .build();
            wo.addItem(target);
        }

        WorkOrder saved = workOrderRepository.save(wo);
        log.info("Work Order {} successfully created from quotation {}", saved.getWorkOrderNo(), quotation.getQuotationNo());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponse getById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        WorkOrder wo = workOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new WorkOrderException("Work Order not found with ID: " + id));
        return toResponse(wo);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponse getByNumber(String workOrderNo) {
        WorkOrder wo = workOrderRepository.findByWorkOrderNo(workOrderNo)
                .orElseThrow(() -> new WorkOrderException("Work Order not found with Number: " + workOrderNo));
        return toResponse(wo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> list(String status, Pageable pageable) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (status != null && !status.isBlank()) {
            try {
                WorkOrderStatus statusEnum = WorkOrderStatus.valueOf(status.toUpperCase());
                return workOrderRepository.findAll((root, query, cb) -> cb.and(
                        cb.equal(root.get("company").get("id"), companyId),
                        cb.equal(root.get("status"), statusEnum)
                ), pageable).map(this::toResponse);
            } catch (IllegalArgumentException e) {
                throw new WorkOrderException("Invalid status: " + status);
            }
        }
        return workOrderRepository.findAll((root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId), pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public WorkOrderResponse updateStatus(UUID id, UpdateWorkOrderStatusRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        WorkOrder wo = workOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new WorkOrderException("Work Order not found with ID: " + id));

        WorkOrderStatus newStatus;
        try {
            newStatus = WorkOrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new WorkOrderException("Invalid status: " + request.getStatus());
        }

        WorkOrderStatus currentStatus = wo.getStatus();
        if (currentStatus == newStatus) {
            return toResponse(wo);
        }

        // Validate transitions
        if (currentStatus == WorkOrderStatus.COMPLETED || currentStatus == WorkOrderStatus.CANCELLED) {
            throw new WorkOrderException("Cannot change status of a terminal Work Order in status: " + currentStatus);
        }

        wo.setStatus(newStatus);
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            wo.setRemarks((wo.getRemarks() == null ? "" : wo.getRemarks() + "\n") 
                    + "[" + newStatus + "] " + request.getRemarks());
        }

        WorkOrder saved = workOrderRepository.save(wo);
        log.info("Work Order {} status updated to {}", saved.getWorkOrderNo(), newStatus);
        return toResponse(saved);
    }

    private WorkOrderResponse toResponse(WorkOrder wo) {
        List<WorkOrderItemResponse> itemResponses = wo.getItems().stream()
                .map(i -> {
                    int totalQty = i.getQuantity() != null ? i.getQuantity() : 0;
                    int alreadyInvoiced = salesInvoiceItemRepository.getSumQuantityByWorkOrderItemId(i.getId());
                    int remaining = Math.max(0, totalQty - alreadyInvoiced);

                    return WorkOrderItemResponse.builder()
                            .id(i.getId())
                            .lineNumber(i.getLineNumber())
                            .quotationItemId(i.getQuotationItem() != null ? i.getQuotationItem().getId() : null)
                            .orderType(i.getOrderType().name())
                            .toolName(i.getToolName())
                            .itemName(i.getItemName())
                            .quantity(totalQty)
                            .alreadyInvoicedQuantity(alreadyInvoiced)
                            .remainingUninvoicedQuantity(remaining)
                            .unitPrice(i.getQuotationItem() != null ? i.getQuotationItem().getUnitPrice() : null)
                            .trial(i.getTrial())
                            .trialStatus(i.getTrialStatus() != null ? i.getTrialStatus().name() : null)
                            .trialFeedback(i.getTrialFeedback())
                            .itemRemarks(i.getItemRemarks())
                            .drawingReference(i.getDrawingReference())
                            .materialType(i.getMaterialType())
                            .materialGrade(i.getMaterialGrade() != null ? i.getMaterialGrade().name() : null)
                            .coatingRequired(i.getCoatingRequired())
                            .coatingType(i.getCoatingType())
                            .resharpeningType(i.getResharpeningType())
                            .diameter(i.getDiameter())
                            .fluteLength(i.getFluteLength())
                            .shankDiameter(i.getShankDiameter())
                            .overallLength(i.getOverallLength())
                            .technicalNotes(i.getTechnicalNotes())
                            .damageLevel(i.getDamageLevel())
                            .specialGeometry(i.getSpecialGeometry())
                            .specialProfile(i.getSpecialProfile())
                            .expressDelivery(i.getExpressDelivery())
                            .build();
                })
                .collect(Collectors.toList());

        return WorkOrderResponse.builder()
                .id(wo.getId())
                .workOrderNo(wo.getWorkOrderNo())
                .quotationId(wo.getQuotation() != null ? wo.getQuotation().getId() : null)
                .quotationNo(wo.getQuotation() != null ? wo.getQuotation().getQuotationNo() : null)
                .status(wo.getStatus().name())
                .customerId(wo.getCustomer().getId())
                .customerCompanyName(wo.getCustomerCompanyName())
                .customerContactPerson(wo.getCustomerContactPerson())
                .customerEmail(wo.getCustomerEmail())
                .customerMobile(wo.getCustomerMobile())
                .remarks(wo.getRemarks())
                .plannedStartDate(wo.getPlannedStartDate())
                .plannedEndDate(wo.getPlannedEndDate())
                .expectedDeliveryDate(wo.getExpectedDeliveryDate())
                .poNumber(wo.getPoNumber())
                .shippingAddress(wo.getShippingAddress() != null ? wo.getShippingAddress() : (wo.getCustomer() != null ? wo.getCustomer().getDeliveryAddress() : null))
                .items(itemResponses)
                .createdAt(wo.getCreatedAt())
                .createdBy(wo.getCreatedBy())
                .updatedAt(wo.getUpdatedAt())
                .updatedBy(wo.getUpdatedBy())
                .companyId(wo.getCompany().getId())
                .companyCode(wo.getCompany().getCode())
                .build();
    }

    @Override
    @Transactional
    public WorkOrderResponse updateTrialResult(UUID itemId, UpdateTrialResultRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        WorkOrderItem item = workOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Work Order Item not found with ID: " + itemId));

        if (!item.getWorkOrder().getCompany().getId().equals(companyId)) {
            throw new BusinessException("Work Order Item does not belong to the active company context");
        }

        if (!Boolean.TRUE.equals(item.getTrial())) {
            throw new BusinessException("Item " + item.getToolName() + " is not a trial item");
        }

        item.setTrialStatus(request.getStatus());
        item.setTrialFeedback(request.getFeedback());
        workOrderItemRepository.save(item);
        
        log.info("Trial status for item {} updated to {}", itemId, request.getStatus());
        return toResponse(item.getWorkOrder());
    }

    @Override
    @Transactional
    public WorkOrderResponse updatePlanning(UUID id, UpdateWorkOrderPlanningRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        WorkOrder wo = workOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new WorkOrderException("Work Order not found with ID: " + id));

        wo.setPlannedStartDate(request.getPlannedStartDate());
        wo.setPlannedEndDate(request.getPlannedEndDate());
        WorkOrder saved = workOrderRepository.save(wo);

        log.info("Work Order {} planning dates updated by production planner: Start={}, End={}",
                saved.getWorkOrderNo(), request.getPlannedStartDate(), request.getPlannedEndDate());
        return toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> searchWorkOrders(String query, Pageable pageable) {
        UUID companyId = com.kalibyte.YashTools.common.multi_company.CompanyContextHolder.getCompanyId();
        Page<WorkOrder> result = workOrderRepository.searchWorkOrders(companyId, query, pageable);
        return result.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse getProgress(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        WorkOrder wo = workOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Work Order not found with ID: " + id));

        List<com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse.WorkOrderItemProgressResponse> itemProgresses = wo.getItems().stream().map(item -> {
            List<com.kalibyte.YashTools.production.jobcard.entity.JobCard> jobCards = jobCardRepository.findByWorkOrderItemIdAndCompanyId(item.getId(), companyId);

            int plannedQty = jobCards.stream().mapToInt(com.kalibyte.YashTools.production.jobcard.entity.JobCard::getTotalQuantity).sum();

            List<com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse.JobCardSummaryResponse> jcSummaries = jobCards.stream().map(jc -> 
                com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse.JobCardSummaryResponse.builder()
                        .jobCardId(jc.getId())
                        .jobCardNo(jc.getJobCardNo())
                        .status(jc.getStatus().name())
                        .isRework(Boolean.TRUE.equals(jc.getIsRework()))
                        .quantity(jc.getTotalQuantity())
                        .build()
            ).collect(Collectors.toList());

            int producedQty = 0;
            int acceptedQty = 0;
            int rejectedQty = 0;
            int reworkQty = 0;

            for (com.kalibyte.YashTools.production.jobcard.entity.JobCard jc : jobCards) {
                List<com.kalibyte.YashTools.production.execution.entity.ExecutionLog> logs = executionLogRepository.findByJobCardId(jc.getId());
                producedQty += logs.stream().mapToInt(com.kalibyte.YashTools.production.execution.entity.ExecutionLog::getProducedQuantity).sum();

                java.util.Optional<com.kalibyte.YashTools.production.tracking.entity.QualityInspection> qi = qualityInspectionRepository.findByJobCardIdAndCompanyId(jc.getId(), companyId);
                if (qi.isPresent()) {
                    acceptedQty += qi.get().getAcceptedQuantity();
                    rejectedQty += qi.get().getRejectedQuantity();
                    reworkQty += qi.get().getReworkQuantity();
                }
            }

            List<com.kalibyte.YashTools.production.packing.entity.PackingLog> packingLogs = packingLogRepository.findByWorkOrderItemIdAndCompanyId(item.getId(), companyId);
            int packedQty = packingLogs.stream().mapToInt(com.kalibyte.YashTools.production.packing.entity.PackingLog::getQuantity).sum();

            int deliveredQty = deliveryChallanItemRepository.getSumQuantityByWorkOrderItemId(item.getId());

            return com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse.WorkOrderItemProgressResponse.builder()
                    .itemId(item.getId())
                    .toolName(item.getToolName())
                    .itemName(item.getItemName())
                    .orderedQuantity(item.getQuantity())
                    .diameter(item.getDiameter())
                    .shankDiameter(item.getShankDiameter())
                    .overallLength(item.getOverallLength())
                    .fluteLength(item.getFluteLength())
                    .drawingReference(item.getDrawingReference())
                    .materialGrade(item.getMaterialGrade() != null ? item.getMaterialGrade().name() : null)
                    .materialType(item.getMaterialType())
                    .coatingType(item.getCoatingType())
                    .technicalNotes(item.getTechnicalNotes())
                    .trial(item.getTrial())
                    .trialStatus(item.getTrialStatus() != null ? item.getTrialStatus().name() : null)
                    .plannedQuantity(plannedQty)
                    .producedQuantity(producedQty)
                    .acceptedQuantity(acceptedQty)
                    .rejectedQuantity(rejectedQty)
                    .reworkQuantity(reworkQty)
                    .packedQuantity(packedQty)
                    .deliveredQuantity(deliveredQty)
                    .jobCards(jcSummaries)
                    .build();
        }).collect(Collectors.toList());

        return com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse.builder()
                .workOrderId(wo.getId())
                .workOrderNo(wo.getWorkOrderNo())
                .status(wo.getStatus().name())
                .customerCompanyName(wo.getCustomerCompanyName())
                .expectedDeliveryDate(wo.getExpectedDeliveryDate())
                .items(itemProgresses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LockedQuotationSummaryResponse> getLockedQuotationsForCustomer(UUID customerId) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        List<Quotation> quotations = quotationRepository.findLockedQuotationsForCustomer(companyId, customerId);
        return quotations.stream().map(this::toLockedSummary).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LockedQuotationSummaryResponse> getAllLockedQuotationsAvailable() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        List<Quotation> quotations = quotationRepository.findAllLockedQuotationsAvailable(companyId);
        return quotations.stream().map(this::toLockedSummary).collect(Collectors.toList());
    }

    private LockedQuotationSummaryResponse toLockedSummary(Quotation q) {
        return LockedQuotationSummaryResponse.builder()
                .quotationId(q.getId())
                .quotationNo(q.getQuotationNo())
                .version(q.getVersion())
                .status(q.getStatus().name())
                .customerId(q.getCustomer().getId())
                .customerCompanyName(q.getCustomerCompanyName())
                .customerContactPerson(q.getCustomerContactPerson())
                .grandTotal(q.getGrandTotal())
                .currency(q.getCurrency())
                .itemCount(q.getItems() != null ? q.getItems().size() : 0)
                .lockedAt(q.getLockedAt())
                .lockedBy(q.getLockedBy())
                .createdAt(q.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingDispatchWorkOrderResponse> getPendingDispatchWorkOrders(UUID customerId) {
        UUID companyId = CompanyContextHolder.getCompanyId();

        List<WorkOrderStatus> activeStatuses = List.of(
                WorkOrderStatus.CREATED,
                WorkOrderStatus.IN_PROGRESS,
                WorkOrderStatus.PRODUCTION_COMPLETED,
                WorkOrderStatus.COMPLETED
        );

        List<WorkOrder> workOrders;
        if (customerId != null) {
            workOrders = workOrderRepository.findByCompanyIdAndStatusInAndCustomerId(
                    companyId, activeStatuses, customerId);
        } else {
            workOrders = workOrderRepository.findByCompanyIdAndStatusIn(
                    companyId, activeStatuses);
        }

        return workOrders.stream()
                .map(this::toPendingDispatchResponse)
                .filter(r -> r != null && r.getItems() != null && !r.getItems().isEmpty())
                .collect(Collectors.toList());
    }

    private PendingDispatchWorkOrderResponse toPendingDispatchResponse(WorkOrder wo) {
        List<PendingDispatchWorkOrderResponse.PendingDispatchItemResponse> pendingItems = wo.getItems().stream()
                .map(item -> {
                    int orderedQty = item.getQuantity();
                    int dispatchedQty = deliveryChallanItemRepository.getSumQuantityByWorkOrderItemId(item.getId());
                    int remainingQty = Math.max(0, orderedQty - dispatchedQty);

                    if (remainingQty <= 0) {
                        return null;
                    }

                    return PendingDispatchWorkOrderResponse.PendingDispatchItemResponse.builder()
                            .workOrderItemId(item.getId())
                            .toolName(item.getToolName())
                            .orderedQuantity(orderedQty)
                            .dispatchedQuantity(dispatchedQty)
                            .remainingQuantity(remainingQty)
                            .unitPrice(item.getQuotationItem() != null ? item.getQuotationItem().getUnitPrice() : null)
                            .diameter(item.getDiameter())
                            .fluteLength(item.getFluteLength())
                            .shankDiameter(item.getShankDiameter())
                            .overallLength(item.getOverallLength())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (pendingItems.isEmpty()) {
            return null;
        }

        return PendingDispatchWorkOrderResponse.builder()
                .workOrderId(wo.getId())
                .workOrderNumber(wo.getWorkOrderNo())
                .customerId(wo.getCustomer().getId())
                .customerName(wo.getCustomerContactPerson())
                .companyName(wo.getCustomerCompanyName())
                .items(pendingItems)
                .build();
    }
}
