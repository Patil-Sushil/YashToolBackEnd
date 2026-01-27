package com.example.YashToolBackEnd.enquiry.entity;

import com.example.YashToolBackEnd.common.enums.OrderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "enquiry_items")
@Getter
@Setter
public class EnquiryItem {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Enquiry enquiry;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private String toolName;
    private Boolean isTrial;
    private Integer quantity;
    private String remarks;

    @OneToOne(mappedBy = "enquiryItem",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private NewToolSpecs newToolSpecs;


    @OneToOne(mappedBy = "enquiryItem", cascade = CascadeType.ALL)
    private ResharpeningSpecs resharpeningSpecs;

    @OneToOne(mappedBy = "enquiryItem", cascade = CascadeType.ALL)
    private ReformingSpecs reformingSpecs;



}
