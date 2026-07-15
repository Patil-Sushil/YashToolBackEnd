package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_invoice_items")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"purchaseInvoice"})
@ToString(exclude = {"purchaseInvoice"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoiceItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_grade_id")
    private MaterialGrade materialGrade;

    @Column(name = "invoice_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal invoiceQuantity;

    @Column(name = "invoice_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal invoiceRate;

    @Builder.Default
    @Column(name = "gst", nullable = false, precision = 19, scale = 4)
    private BigDecimal gst = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "taxable_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxableAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
