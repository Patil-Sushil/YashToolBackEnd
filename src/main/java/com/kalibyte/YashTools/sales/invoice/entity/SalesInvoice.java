package com.kalibyte.YashTools.sales.invoice.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_invoices")
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesInvoice extends BaseCompanyEntity {

    @Column(name = "invoice_no", nullable = false, length = 30, unique = true)
    private String invoiceNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "sub_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal subTotal;

    @Column(name = "cgst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal cgstRate;

    @Column(name = "cgst_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal cgstAmount;

    @Column(name = "sgst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal sgstRate;

    @Column(name = "sgst_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal sgstAmount;

    @Column(name = "igst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal igstRate;

    @Column(name = "igst_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal igstAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, PAID, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SalesInvoiceItem> items = new ArrayList<>();

    public void addItem(SalesInvoiceItem item) {
        items.add(item);
        item.setSalesInvoice(this);
    }
}
