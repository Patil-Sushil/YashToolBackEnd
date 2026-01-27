package com.example.YashToolBackEnd.master.coating.entity;

import com.example.YashToolBackEnd.common.base.BaseEntity;
import com.example.YashToolBackEnd.common.enums.CoatingType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coatings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coating extends BaseEntity {

    @Column(nullable = false,unique = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "coating_type", nullable = false)
    private CoatingType coatingType;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private Boolean active;


}
