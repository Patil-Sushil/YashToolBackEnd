package com.example.YashToolBackEnd.enquiry.entity;


import com.example.YashToolBackEnd.common.enums.CoatingType;
import com.example.YashToolBackEnd.common.enums.ResharpeningType;
import com.example.YashToolBackEnd.master.coating.entity.Coating;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "resharpening_specs")
@Getter @Setter
public class ResharpeningSpecs {

    @Id @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(unique = true)
    private EnquiryItem enquiryItem;

    @Enumerated(EnumType.STRING)
    private ResharpeningType resharpeningType;


    @Column(nullable = false)
    private Boolean hasCoating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coating_id")
    private Coating coating;


}
