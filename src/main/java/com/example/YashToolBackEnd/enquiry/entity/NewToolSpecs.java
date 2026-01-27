package com.example.YashToolBackEnd.enquiry.entity;

import com.example.YashToolBackEnd.master.coating.entity.Coating;
import com.example.YashToolBackEnd.master.rawmaterial.entity.RawMaterial;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "new_tool_specs")
@Getter
@Setter
public class NewToolSpecs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  Correct relationship to EnquiryItem
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_item_id", nullable = false, unique = true)
    private EnquiryItem enquiryItem;

    //  SIMPLE BOOLEAN (NO RELATIONSHIP)
    @Column(nullable = false)
    private Boolean hasCoating;

    //  ACTUAL RELATIONSHIP TO COATING MASTER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coating_id")
    private Coating coating;

    //  RAW MATERIAL IS MANDATORY
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raw_material_id", nullable = false)
    private RawMaterial rawMaterial;

    @Column(nullable = false)
    private Double diameter;

    @Column(nullable = false)
    private Double fluteLength;

    @Column(nullable = false)
    private Double shankDiameter;

    @Column(nullable = false)
    private Double overallLength;
}
