package com.kalibyte.YashTools.enquiry.entity;

import com.kalibyte.YashTools.common.enums.CoatingType;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialType;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Technical specifications for new tool manufacturing orders
 */
@Entity
@Table(name = "new_tool_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewToolSpecs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_item_id", nullable = false, unique = true)
    private EnquiryItem enquiryItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 30)
    private MaterialType materialType;


    @Column(name = "coating_required", nullable = false)
    @Builder.Default
    private Boolean coatingRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "coating_type", length = 30)
    private CoatingType coatingType;

    @Column(nullable = false)
    private Double diameter;

    @Column(name = "flute_length", nullable = false)
    private Double fluteLength;

    @Column(name = "shank_diameter", nullable = false)
    private Double shankDiameter;

    @Column(name = "overall_length", nullable = false)
    private Double overallLength;

    @Column(name = "technical_notes", columnDefinition = "TEXT")
    private String technicalNotes;
}