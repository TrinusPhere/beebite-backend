package com.fidecode.beebite.domain.catalog.entity;

import com.fidecode.beebite.domain.catalog.valueobject.CatalogItemType;
import com.fidecode.beebite.domain.catalog.valueobject.ChannelPrice;
import com.fidecode.beebite.domain.catalog.valueobject.ProductStatus;
import com.fidecode.beebite.domain.catalog.valueobject.ProductType;
import com.fidecode.beebite.domain.catalog.valueobject.Unit;
import com.fidecode.beebite.domain.shared.StoreGuard;
import java.math.BigDecimal;
import java.util.*;

public class Product implements StoreGuard.StoreScoped {
    private UUID id;
    private Long version;
    private final UUID storeId;
    private String name;
    private UUID categoryId;
    private String description;
    private BigDecimal price;
    private BigDecimal estimatedCost;
    private String imageUrl;
    private int stock;
    private ProductStatus status;
    private String externalCode;
    private CatalogItemType catalogItemType;
    private List<ChannelPrice> channelPrices;
    private ProductType productType;
    private boolean trackStock;
    private UUID productionSectorId;
    private Unit saleUnit;
    private boolean availableDigitalMenu;
    private boolean availablePdv;
    private List<TechnicalSheetItem> technicalSheet;
    private List<UUID> removableItemIds;
    private List<UUID> modifierGroupIds;

    public Product(UUID storeId, String name, UUID categoryId, BigDecimal price) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price != null ? price : BigDecimal.ZERO;
        status = ProductStatus.AVAILABLE;
        catalogItemType = CatalogItemType.DEFAULT;
        channelPrices = new ArrayList<>();
        productType = ProductType.PRODUCED;
        saleUnit = Unit.UN;
        availableDigitalMenu = true;
        availablePdv = true;
        technicalSheet = new ArrayList<>();
        removableItemIds = new ArrayList<>();
        modifierGroupIds = new ArrayList<>();
    }

    public static Product reconstitute(UUID id, Long version, UUID storeId, String name, UUID categoryId,
            String description, BigDecimal price, BigDecimal estimatedCost, String imageUrl, int stock, ProductStatus status, String externalCode,
            CatalogItemType catalogItemType, List<ChannelPrice> channelPrices, ProductType productType,
            boolean trackStock, UUID productionSectorId, Unit saleUnit, boolean availableDigitalMenu,
            boolean availablePdv, List<TechnicalSheetItem> technicalSheet, List<UUID> removableItemIds,
            List<UUID> modifierGroupIds) {
        Product p = new Product(storeId, name, categoryId, price);
        p.id = id;
        p.version = version;
        p.description = description;
        p.estimatedCost = estimatedCost;
        p.imageUrl = imageUrl;
        p.initializeStock(stock);
        p.status = status != null ? status : ProductStatus.AVAILABLE;
        p.externalCode = externalCode;
        p.catalogItemType = catalogItemType != null ? catalogItemType : CatalogItemType.DEFAULT;
        p.channelPrices = channelPrices != null ? new ArrayList<>(channelPrices) : new ArrayList<>();
        p.productType = productType != null ? productType : ProductType.PRODUCED;
        p.trackStock = trackStock;
        p.productionSectorId = productionSectorId;
        p.saleUnit = saleUnit != null ? saleUnit : Unit.UN;
        p.availableDigitalMenu = availableDigitalMenu;
        p.availablePdv = availablePdv;
        p.replaceTechnicalSheet(technicalSheet);
        p.replaceRemovableItems(removableItemIds);
        p.replaceModifierGroups(modifierGroupIds);
        return p;
    }

    public void updateBasicInfo(String name, UUID categoryId, String description, BigDecimal price, BigDecimal estimatedCost, String imageUrl) {
        if (name != null && !name.isBlank())
            this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        if (price != null)
            this.price = price;
        this.estimatedCost = estimatedCost;
        this.imageUrl = imageUrl;
    }

    public void configureInventory(ProductType type, boolean trackStock, UUID sector, Unit unit) {
        this.productType = type != null ? type : ProductType.PRODUCED;
        this.trackStock = trackStock;
        this.productionSectorId = sector;
        this.saleUnit = unit != null ? unit : Unit.UN;
    }

    public void configureIntegration(String externalCode, CatalogItemType catalogItemType) {
        this.externalCode = externalCode;
        this.catalogItemType = catalogItemType != null ? catalogItemType : CatalogItemType.DEFAULT;
    }
    
    public void replaceChannelPrices(List<ChannelPrice> channelPrices) {
        this.channelPrices = channelPrices != null ? new ArrayList<>(channelPrices) : new ArrayList<>();
    }

    public void configureAvailability(ProductStatus status, boolean digital, boolean pdv) {
        this.status = status != null ? status : ProductStatus.AVAILABLE;
        this.availableDigitalMenu = digital;
        this.availablePdv = pdv;
    }

    public void initializeStock(int value) {
        if (value < 0)
            throw new IllegalArgumentException("Estoque não pode ser negativo.");
        stock = value;
    }

    public void decreaseStock(int q) {
        if (q <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (stock - q < 0)
            throw new IllegalArgumentException("Estoque insuficiente para o produto " + name + ".");
        stock -= q;
    }

    public void increaseStock(int q) {
        if (q <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        stock += q;
    }

    public void replaceTechnicalSheet(List<TechnicalSheetItem> list) {
        technicalSheet = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public void replaceRemovableItems(List<UUID> ids) {
        removableItemIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
    }

    public void replaceModifierGroups(List<UUID> ids) {
        modifierGroupIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
    }

    public BigDecimal calculateTotalCost() {
        if (technicalSheet != null && !technicalSheet.isEmpty()) {
            return technicalSheet.stream().map(TechnicalSheetItem::getLineCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return estimatedCost != null ? estimatedCost : BigDecimal.ZERO;
    }

    public BigDecimal calculateProfit() {
        return price.subtract(calculateTotalCost());
    }

    public BigDecimal calculateNetProfit(BigDecimal channelFeeRate) {
        BigDecimal fee = price.multiply(channelFeeRate != null ? channelFeeRate : BigDecimal.ZERO);
        return price.subtract(calculateTotalCost()).subtract(fee);
    }

    public UUID getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getStock() {
        return stock;
    }

    public ProductStatus getStatus() {
        return status;
    }
    
    public String getExternalCode() {
        return externalCode;
    }

    public CatalogItemType getCatalogItemType() {
        return catalogItemType;
    }

    public List<ChannelPrice> getChannelPrices() {
        return Collections.unmodifiableList(channelPrices);
    }

    public ProductType getProductType() {
        return productType;
    }

    public boolean isTrackStock() {
        return trackStock;
    }

    public UUID getProductionSectorId() {
        return productionSectorId;
    }

    public Unit getSaleUnit() {
        return saleUnit;
    }

    public boolean isAvailableDigitalMenu() {
        return availableDigitalMenu;
    }

    public boolean isAvailablePdv() {
        return availablePdv;
    }

    public List<TechnicalSheetItem> getTechnicalSheet() {
        return Collections.unmodifiableList(technicalSheet);
    }

    public List<UUID> getRemovableItemIds() {
        return Collections.unmodifiableList(removableItemIds);
    }

    public List<UUID> getModifierGroupIds() {
        return Collections.unmodifiableList(modifierGroupIds);
    }

    public void setId(UUID id) {
        this.id = id;
    }
}