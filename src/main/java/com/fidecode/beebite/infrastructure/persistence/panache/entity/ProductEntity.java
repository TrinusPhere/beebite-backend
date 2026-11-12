package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import java.time.LocalDateTime;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "product")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class ProductEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Version
    public Long version;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(nullable = false)
    public String name;

    @Column(name = "category_id")
    public UUID categoryId;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(precision = 10, scale = 2)
    public BigDecimal price;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    public BigDecimal estimatedCost;

    @Column(name = "image_url")
    public String imageUrl;

    public int stock;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "external_code")
    public String externalCode;

    @Column(name = "catalog_item_type", nullable = false)
    public String catalogItemType;

    @Column(name = "product_type", nullable = false)
    public String productType;

    @Column(name = "track_stock", nullable = false)
    public boolean trackStock;

    @Column(name = "production_sector_id")
    public UUID productionSectorId;

    @Column(name = "sale_unit", nullable = false)
    public String saleUnit;

    @Column(name = "available_digital_menu", nullable = false)
    public boolean availableDigitalMenu;

    @Column(name = "available_pdv", nullable = false)
    public boolean availablePdv;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<TechnicalSheetItemEntity> technicalSheet = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ProductChannelPriceEntity> channelPrices = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_removable_item", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "ingredient_id")
    public List<UUID> removableItemIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_modifier_group", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "modifier_group_id")
    public List<UUID> modifierGroupIds = new ArrayList<>();

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}
