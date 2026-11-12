package com.fidecode.beebite.infrastructure.persistence.panache.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_group_id", nullable = false)
    public ModifierGroupEntity modifierGroup;

    @Column(nullable = false)
    public String name;

    @Column(precision = 10, scale = 2)
    public BigDecimal price;

    @Column(name = "sort_order")
    public int sortOrder;

    @Column(name = "linked_product_id")
    public UUID linkedProductId;
}
