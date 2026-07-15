package com.kalibyte.YashTools.purchase.master.vendor.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.purchase.master.paymentterms.entity.PaymentTerms;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(
        name = "purchase_vendors",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "vendor_name"})
        }
)
// @Filter(name = "companyFilter", condition = "company_id = :companyId")
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor extends BaseCompanyEntity {

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "pan", length = 20)
    private String pan;

    @Column(name = "contact_details")
    private String contactDetails;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_terms_id")
    private PaymentTerms paymentTerms;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
