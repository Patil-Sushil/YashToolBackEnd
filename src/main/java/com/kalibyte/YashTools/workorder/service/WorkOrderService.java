package com.kalibyte.YashTools.workorder.service;

import com.kalibyte.YashTools.workorder.dto.request.CreateWorkOrderRequest;
import com.kalibyte.YashTools.workorder.dto.request.UpdateWorkOrderStatusRequest;
import com.kalibyte.YashTools.workorder.dto.response.WorkOrderResponse;
import com.kalibyte.YashTools.workorder.dto.response.LockedQuotationSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface WorkOrderService {
    WorkOrderResponse createFromQuotation(CreateWorkOrderRequest request);
    WorkOrderResponse getById(UUID id);
    WorkOrderResponse getByNumber(String workOrderNo);
    Page<WorkOrderResponse> list(String status, Pageable pageable);
    WorkOrderResponse updateStatus(UUID id, UpdateWorkOrderStatusRequest request);
    WorkOrderResponse updateTrialResult(UUID itemId, com.kalibyte.YashTools.workorder.dto.request.UpdateTrialResultRequest request);
    WorkOrderResponse updatePlanning(UUID id, com.kalibyte.YashTools.workorder.dto.request.UpdateWorkOrderPlanningRequest request);


    Page<WorkOrderResponse> searchWorkOrders(String query, Pageable pageable);
    com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse getProgress(UUID id);
    List<LockedQuotationSummaryResponse> getLockedQuotationsForCustomer(UUID customerId);
    List<LockedQuotationSummaryResponse> getAllLockedQuotationsAvailable();
    List<com.kalibyte.YashTools.workorder.dto.response.PendingDispatchWorkOrderResponse> getPendingDispatchWorkOrders(UUID customerId);
}
