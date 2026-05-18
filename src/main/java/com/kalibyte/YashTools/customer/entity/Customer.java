package com.kalibyte.YashTools.customer.entity;

import com.kalibyte.YashTools.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "mobile_number"),
                @UniqueConstraint(columnNames = "email")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

        @Builder.Default                                    // ← ADD THIS
        @Column(name = "is_deleted", nullable = false)
        private Boolean isDeleted = false;

        @Column(name = "company_name", nullable = false)
        private String companyName;

        @Column(name = "customer_name", nullable = false)
        private String customerName;

        @Column(name = "legal_entity", nullable = false)
        private String legalEntity;

        @Column(name = "business_type", nullable = false)
        private String businessType;

        @Column(nullable = false, length = 15)
        private String mobileNumber;

        @Column(nullable = false, length = 50)
        private String email;

        @Column(name = "billing_address", nullable = false)
        private String billingAddress;

        @Column(name = "delivery_address", nullable = false)
        private String deliveryAddress;

        @Column(length = 20)
        private String gstNumber;

        @Column(nullable = false)
        private String status;
}