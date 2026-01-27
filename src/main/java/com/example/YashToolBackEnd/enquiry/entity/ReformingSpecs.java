package com.example.YashToolBackEnd.enquiry.entity;

import com.example.YashToolBackEnd.master.coating.entity.Coating;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reforming_specs")
@Getter @Setter
public class ReformingSpecs {
    @Id @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(unique = true)
    private EnquiryItem enquiryItem;

    @Column(nullable = false)
    private Boolean hasCoating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coating_id")
    private Coating coating;


    private Double fluteLength;
}
