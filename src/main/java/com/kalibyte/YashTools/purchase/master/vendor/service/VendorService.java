package com.kalibyte.YashTools.purchase.master.vendor.service;

import com.kalibyte.YashTools.purchase.master.vendor.dto.request.VendorRequest;
import com.kalibyte.YashTools.purchase.master.vendor.dto.response.VendorResponse;

import java.util.List;
import java.util.UUID;

public interface VendorService {
    VendorResponse create(VendorRequest request);
    VendorResponse getById(UUID id);
    List<VendorResponse> getAll();
    VendorResponse update(UUID id, VendorRequest request);
    void delete(UUID id);
    java.math.BigDecimal getOutstandingBalance(UUID id);


    List<VendorResponse> searchVendors(String query);

}
