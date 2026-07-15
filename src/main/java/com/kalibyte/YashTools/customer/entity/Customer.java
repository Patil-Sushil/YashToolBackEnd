package com.kalibyte.YashTools.customer.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "mobile_number"}),
                @UniqueConstraint(columnNames = {"company_id", "email"})
        }
)
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseCompanyEntity {

        @Builder.Default
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