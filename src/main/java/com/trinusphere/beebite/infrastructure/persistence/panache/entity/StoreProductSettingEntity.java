package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "store_product_setting")
public class StoreProductSettingEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "product_id", nullable = false)
    public UUID productId;

    @Column(name = "stock", nullable = false)
    public int stock;

    @Column(name = "local_status", nullable = false, length = 20)
    public String localStatus = "AVAILABLE";

    @Column(name = "production_sector_id")
    public UUID productionSectorId;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}

