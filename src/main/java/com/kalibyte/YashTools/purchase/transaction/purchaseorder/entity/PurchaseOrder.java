package com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.purchase.master.paymentterms.entity.PaymentTerms;
import com.kalibyte.YashTools.purchase.master.purchasetype.entity.PurchaseType;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "purchase_orders",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "po_number"})
        }
)
// @Filter(name = "companyFilter", condition = "company_id = :companyId")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"items"})
@ToString(exclude = {"items"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder extends BaseCompanyEntity {

    @Column(name = "po_number", nullable = false)
    private String poNumber;

    @Column(name = "po_date", nullable = false)
    private LocalDate poDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_terms_id")
    private PaymentTerms paymentTerms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_type_id")
    private PurchaseType purchaseType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }

    public void removeItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
    }
}
