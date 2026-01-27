package com.example.YashToolBackEnd.master.rawmaterial.entity;

import com.example.YashToolBackEnd.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "raw_materials")
@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterial extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private Boolean active = true;
}
