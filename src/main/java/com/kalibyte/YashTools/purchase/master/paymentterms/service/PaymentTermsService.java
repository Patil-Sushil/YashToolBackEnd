package com.kalibyte.YashTools.purchase.master.paymentterms.service;

import com.kalibyte.YashTools.purchase.master.paymentterms.dto.request.PaymentTermsRequest;
import com.kalibyte.YashTools.purchase.master.paymentterms.dto.response.PaymentTermsResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentTermsService {
    PaymentTermsResponse create(PaymentTermsRequest request);
    PaymentTermsResponse getById(UUID id);
    List<PaymentTermsResponse> getAll();
    PaymentTermsResponse update(UUID id, PaymentTermsRequest request);
    void delete(UUID id);
}
