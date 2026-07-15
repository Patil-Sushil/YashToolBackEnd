package com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request;

import com.kalibyte.YashTools.purchase.shared.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorPaymentRequest {

    private LocalDate paymentDate;

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String paymentReferenceNumber;

    private String remarks;

    @NotEmpty(message = "Payment must be allocated to at least one purchase invoice")
    @Valid
    private List<VendorPaymentItemRequest> items;
}
