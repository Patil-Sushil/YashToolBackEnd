package com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import com.kalibyte.YashTools.purchase.shared.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "purchase_vendor_payments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "payment_number"})
        }
)
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"items"})
@ToString(exclude = {"items"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPayment extends BaseCompanyEntity {

    @Column(name = "payment_number", nullable = false)
    private String paymentNumber;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference_number")
    private String paymentReferenceNumber;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "vendorPayment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VendorPaymentItem> items = new ArrayList<>();

    public void addItem(VendorPaymentItem item) {
        items.add(item);
        item.setVendorPayment(this);
    }

    public void removeItem(VendorPaymentItem item) {
        items.remove(item);
        item.setVendorPayment(null);
    }
}
