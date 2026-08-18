package com.fidecode.biteflow.domain.catalog.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity — Product.
 * POJO puro, sem dependência de framework.
 */
public class Product implements StoreGuard.StoreScoped {

    private UUID id;
    private Long version; // P9 — campo @Version para controle de concorrência
    private UUID storeId;
    private String name;
    private UUID categoryId;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private int stock;
    private boolean active;
    private List<TechnicalSheetItem> technicalSheet;
    private List<UUID> removableItemIds;
    private List<UUID> modifierGroupIds;

    public Product(UUID storeId, String name, UUID categoryId, BigDecimal price) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.active = true;
        this.technicalSheet = new ArrayList<>();
        this.removableItemIds = new ArrayList<>();
        this.modifierGroupIds = new ArrayList<>();
    }

    /**
     * Calcula o custo total via ficha técnica: sum(costSnapshot × quantity).
     */
    public BigDecimal calculateTotalCost() {
        if (technicalSheet == null || technicalSheet.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return technicalSheet.stream()
            .map(TechnicalSheetItem::getLineCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula o lucro: price - totalCost.
     */
    public BigDecimal calculateProfit() {
        return price.subtract(calculateTotalCost());
    }

    // ── Getters ──
    public UUID getId()                         { return id; }
    public Long getVersion()                    { return version; }
    public UUID getStoreId()                    { return storeId; }
    public String getName()                     { return name; }
    public UUID getCategoryId()                 { return categoryId; }
    public String getDescription()              { return description; }
    public BigDecimal getPrice()                { return price; }
    public String getImageUrl()                 { return imageUrl; }
    public int getStock()                       { return stock; }
    public boolean isActive()                   { return active; }
    public List<TechnicalSheetItem> getTechnicalSheet()  { return technicalSheet; }
    public List<UUID> getRemovableItemIds()     { return removableItemIds; }
    public List<UUID> getModifierGroupIds()     { return modifierGroupIds; }

    // ── Setters ──
    public void setId(UUID id)                                  { this.id = id; }
    public void setVersion(Long version)                        { this.version = version; }
    public void setName(String name)                            { this.name = name; }
    public void setCategoryId(UUID categoryId)                  { this.categoryId = categoryId; }
    public void setDescription(String description)              { this.description = description; }
    public void setPrice(BigDecimal price)                      { this.price = price; }
    public void setImageUrl(String imageUrl)                    { this.imageUrl = imageUrl; }
    public void setStock(int stock)                             { this.stock = stock; }
    public void setActive(boolean active)                       { this.active = active; }
    public void setTechnicalSheet(List<TechnicalSheetItem> ts)  { this.technicalSheet = ts != null ? ts : new ArrayList<>(); }
    public void setRemovableItemIds(List<UUID> ids)             { this.removableItemIds = ids != null ? ids : new ArrayList<>(); }
    public void setModifierGroupIds(List<UUID> ids)             { this.modifierGroupIds = ids != null ? ids : new ArrayList<>(); }
}
