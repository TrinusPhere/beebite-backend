package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "modifier_option")
public class ModifierOptionEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "modifier_group_id", nullable = false)
    public UUID modifierGroupId;

    @Column(nullable = false)
    public String name;

    @Column(precision = 10, scale = 2)
    public BigDecimal price;

    @Column(name = "sort_order")
    public int sortOrder;
}
