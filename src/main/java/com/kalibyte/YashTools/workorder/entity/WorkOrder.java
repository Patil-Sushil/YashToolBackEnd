package com.kalibyte.YashTools.workorder.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_orders", indexes = {
        @Index(name = "idx_work_order_no", columnList = "work_order_no"),
        @Index(name = "idx_work_order_status", columnList = "status"),
        @Index(name = "idx_work_order_company", columnList = "company_id"),
        @Index(name = "idx_work_order_quotation", columnList = "quotation_id")
})
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkOrder extends BaseCompanyEntity {

    @Column(name = "work_order_no", nullable = false, length = 30, unique = true)
    private String workOrderNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false, unique = true)
    private Quotation quotation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private WorkOrderStatus status = WorkOrderStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "customer_company_name")
    private String customerCompanyName;

    @Column(name = "customer_contact_person")
    private String customerContactPerson;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_mobile")
    private String customerMobile;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkOrderItem> items = new ArrayList<>();

    public void addItem(WorkOrderItem item) {
        items.add(item);
        item.setWorkOrder(this);
    }

    public void removeItem(WorkOrderItem item) {
        items.remove(item);
        item.setWorkOrder(null);
    }
}
