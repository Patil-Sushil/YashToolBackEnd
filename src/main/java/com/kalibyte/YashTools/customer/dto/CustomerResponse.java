package com.kalibyte.YashTools.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CustomerResponse {

    private UUID id;
    private String companyName;
    private String customerName;
    private String legalEntity;
    private String businessType;
    private String mobileNumber;
    private String email;
    private String billingAddress;
    private String deliveryAddress;
    private String gstNumber;
    private String status;
}
