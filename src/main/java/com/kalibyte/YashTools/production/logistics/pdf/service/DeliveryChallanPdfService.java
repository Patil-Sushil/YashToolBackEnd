package com.kalibyte.YashTools.production.logistics.pdf.service;

import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;

import java.util.UUID;

public interface DeliveryChallanPdfService {
    byte[] generatePdf(UUID id);
    byte[] generatePdf(DeliveryChallanResponse challan);
}
