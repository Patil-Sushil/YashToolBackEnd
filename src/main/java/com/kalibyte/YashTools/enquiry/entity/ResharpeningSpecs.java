package com.kalibyte.YashTools.enquiry.entity;

import com.kalibyte.YashTools.common.enums.CoatingType;
import com.kalibyte.YashTools.common.enums.ResharpeningType;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "resharpening_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResharpeningSpecs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_item_id", nullable = false, unique = true)
    private EnquiryItem enquiryItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "resharpening_type", nullable = false, length = 20)
    private ResharpeningType resharpeningType;

    @Column(name = "coating_required", nullable = false)
    @Builder.Default
    private Boolean coatingRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "coating_type", length = 30)
    private CoatingType coatingType;

    @Column(name = "technical_notes", columnDefinition = "TEXT")
    private String technicalNotes;
}