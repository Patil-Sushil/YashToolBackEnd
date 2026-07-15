package com.kalibyte.YashTools.production.execution.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.labors.labor.entity.Laborer;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.machine.entity.Machine;
import com.kalibyte.YashTools.production.planning.entity.enums.ShiftType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "execution_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExecutionLog extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_card_id", nullable = false)
    private JobCard jobCard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private Laborer operator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShiftType shift;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "target_quantity", nullable = false)
    private Integer targetQuantity;

    @Column(name = "produced_quantity", nullable = false)
    @Builder.Default
    private Integer producedQuantity = 0;

    @Column(name = "rejected_quantity", nullable = false)
    @Builder.Default
    private Integer rejectedQuantity = 0;

    @Column(name = "rework_quantity", nullable = false)
    @Builder.Default
    private Integer reworkQuantity = 0;

    @Column(name = "pending_quantity", nullable = false)
    @Builder.Default
    private Integer pendingQuantity = 0;

    @Column(name = "machine_downtime_minutes", nullable = false)
    @Builder.Default
    private Integer machineDowntimeMinutes = 0;

    @Column(name = "downtime_reason")
    private String downtimeReason;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
