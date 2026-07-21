package com.kalibyte.YashTools.production.logistics.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_challans")
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryChallan extends BaseCompanyEntity {

    @Column(name = "challan_no", nullable = false, length = 30, unique = true)
    private String challanNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Column(name = "vehicle_no", length = 50)
    private String vehicleNo;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "driver_contact", length = 50)
    private String driverContact;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, DISPATCHED, DELIVERED, CANCELLED

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_receipt_by")
    private String deliveryReceiptBy;

    @Column(name = "delivery_receipt_at")
    private LocalDateTime deliveryReceiptAt;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "deliveryChallan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeliveryChallanItem> items = new ArrayList<>();

    public void addItem(DeliveryChallanItem item) {
        items.add(item);
        item.setDeliveryChallan(this);
    }
}
