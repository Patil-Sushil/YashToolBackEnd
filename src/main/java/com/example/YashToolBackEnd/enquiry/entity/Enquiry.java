package com.example.YashToolBackEnd.enquiry.entity;


import com.example.YashToolBackEnd.common.base.AuditableEntity;
import com.example.YashToolBackEnd.common.enums.EnquiryStatus;
import com.example.YashToolBackEnd.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enquiries")
@Getter @Setter
public class Enquiry extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String enquiryNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Customer customer;

    @Column(name = "is_urgent", nullable = false)
    private Boolean isUrgent = false;

    @Enumerated(EnumType.STRING)
    private EnquiryStatus status;
    private String remarks;

    @OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnquiryItem> items = new ArrayList<>();

}
