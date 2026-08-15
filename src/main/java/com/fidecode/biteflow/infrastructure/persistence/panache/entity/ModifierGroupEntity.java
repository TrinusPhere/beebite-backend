package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "modifier_group")
public class ModifierGroupEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String type;

    public boolean required;

    @Column(name = "min_selections")
    public int minSelections;

    @Column(name = "max_selections")
    public int maxSelections;

    public String icon;

    @OneToMany(mappedBy = "modifierGroupId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<ModifierOptionEntity> options = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
