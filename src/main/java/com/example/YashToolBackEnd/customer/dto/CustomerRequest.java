package com.example.YashToolBackEnd.customer.dto;

import com.example.YashToolBackEnd.common.enums.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    @JsonProperty("companyName")
    @JsonAlias({"company_name", "companyname"})  //  Accepts multiple formats
    private String companyName;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    @JsonProperty("customerName")
    @JsonAlias({"customer_name", "customername"})
    private String customerName;

    @NotBlank(message = "Legal entity is required")
    @JsonProperty("legalEntity")
    @JsonAlias({"legal_entity", "legalentity"})
    private String legalEntity;

    @NotBlank(message = "Business type is required")
    @JsonProperty("businessType")
    @JsonAlias({"business_type", "businesstype"})
    private String businessType;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    @JsonProperty("mobileNumber")
    @JsonAlias({"mobile_number", "mobilenumber", "phone"})
    private String mobileNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Billing address is required")
    @JsonProperty("billingAddress")
    @JsonAlias({"billing_address", "billingaddress"})
    private String billingAddress;

    @JsonProperty("deliveryAddress")
    @JsonAlias({"delivery_address", "deliveryaddress"})
    private String deliveryAddress;

//    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
//            message = "Invalid GST number format")
    @JsonProperty("gstNumber")
    @JsonAlias({"gst_number", "gstnumber", "gst"})
    private String gstNumber;

    private CustomerStatus status = CustomerStatus.ACTIVE;
}