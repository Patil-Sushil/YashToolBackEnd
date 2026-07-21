package com.kalibyte.YashTools.production.logistics.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanRequest;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryReceiptRequest;

import java.util.UUID;

public interface DeliveryChallanService {
    DeliveryChallanResponse createChallan(DeliveryChallanRequest request);
    DeliveryChallanResponse getChallanById(UUID id);
    DeliveryChallanResponse dispatchChallan(UUID id);
    DeliveryChallanResponse recordDeliveryReceipt(UUID id, DeliveryReceiptRequest request);
    PageResponse<DeliveryChallanResponse> getAllChallans(int page, int size);
}
