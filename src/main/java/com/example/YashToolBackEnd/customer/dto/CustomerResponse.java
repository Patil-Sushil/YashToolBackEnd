package com.example.YashToolBackEnd.customer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {

    private Long id;
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
