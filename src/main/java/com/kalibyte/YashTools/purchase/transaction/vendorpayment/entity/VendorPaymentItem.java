package com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_vendor_payment_items")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"vendorPayment"})
@ToString(exclude = {"vendorPayment"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPaymentItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_payment_id", nullable = false)
    private VendorPayment vendorPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name = "invoice_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal invoiceAmount = BigDecimal.ZERO;

    @Column(name = "outstanding_before_payment", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal outstandingBeforePayment = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "outstanding_after_payment", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal outstandingAfterPayment = BigDecimal.ZERO;
}
