package com.kalibyte.YashTools.production.logistics.email;

import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;

import java.util.List;
import java.util.UUID;

public interface DeliveryChallanEmailHandler {
    UUID sendChallanEmail(DeliveryChallanResponse challan);
    UUID sendChallanEmail(DeliveryChallanResponse challan, byte[] pdfBytes);
    UUID sendChallanEmail(DeliveryChallanResponse challan, byte[] pdfBytes, List<String> ccList);
}
