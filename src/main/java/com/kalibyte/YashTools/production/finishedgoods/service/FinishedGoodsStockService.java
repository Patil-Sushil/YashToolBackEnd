package com.kalibyte.YashTools.production.finishedgoods.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.finishedgoods.dto.FinishedGoodsStockResponse;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;

import java.util.UUID;

public interface FinishedGoodsStockService {
    void addFinishedGoodsStock(WorkOrderItem workOrderItem, Integer quantity);
    void deductFinishedGoodsStock(WorkOrderItem workOrderItem, Integer quantity);
    FinishedGoodsStockResponse getStockByWorkOrderItem(UUID workOrderItemId);
    PageResponse<FinishedGoodsStockResponse> getAllFinishedGoodsStock(int page, int size);
}
