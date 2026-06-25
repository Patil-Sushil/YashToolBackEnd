package com.kalibyte.YashTools.enquiry.entity;

import com.kalibyte.YashTools.common.enums.CoatingType;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Specifications for tool reforming/modification services
 */
@Entity
@Table(name = "reforming_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReformingSpecs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_item_id", nullable = false, unique = true)
    private EnquiryItem enquiryItem;

    @Column(name = "coating_required", nullable = false)
    @Builder.Default
    private Boolean coatingRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "coating_type", length = 30)
    private CoatingType coatingType;

    @Column(name = "flute_length", nullable = false)
    private Double fluteLength;

    @Column(name = "technical_notes", columnDefinition = "TEXT")
    private String technicalNotes;
}