package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import com.kalibyte.YashTools.purchase.shared.enums.PaymentStatus;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceipt;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "purchase_invoices",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "invoice_number"}),
                @UniqueConstraint(columnNames = {"company_id", "supplier_invoice_number"})
        }
)
// @Filter(name = "companyFilter", condition = "company_id = :companyId")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"goodsReceipts", "items"})
@ToString(exclude = {"goodsReceipts", "items"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoice extends BaseCompanyEntity {

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name = "supplier_invoice_number", nullable = false)
    private String supplierInvoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "purchase_invoice_grns",
            joinColumns = @JoinColumn(name = "invoice_id"),
            inverseJoinColumns = @JoinColumn(name = "goods_receipt_id")
    )
    @Builder.Default
    private List<GoodsReceipt> goodsReceipts = new ArrayList<>();

    @Column(name = "gst_details", columnDefinition = "TEXT")
    private String gstDetails;

    @Column(name = "freight", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal freight = BigDecimal.ZERO;

    @Column(name = "other_charges", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal otherCharges = BigDecimal.ZERO;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "outstanding_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal outstandingAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PurchaseInvoiceStatus status = PurchaseInvoiceStatus.APPROVED;

    @OneToMany(mappedBy = "purchaseInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseInvoiceItem> items = new ArrayList<>();

    public void addItem(PurchaseInvoiceItem item) {
        items.add(item);
        item.setPurchaseInvoice(this);
    }

    public void removeItem(PurchaseInvoiceItem item) {
        items.remove(item);
        item.setPurchaseInvoice(null);
    }
}
