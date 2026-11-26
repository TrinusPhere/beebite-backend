package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse.*;
import com.trinusphere.beebite.application.sync.port.EdgeCatalogSyncApplier;
import com.trinusphere.beebite.infrastructure.persistence.panache.repository.StoreCatalogContextSupport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class PanacheEdgeCatalogSyncApplier implements EdgeCatalogSyncApplier {

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    EntityManager em;

    @Inject
    StoreCatalogContextSupport catalogSupport;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public EdgeCatalogDeltaResponse buildDeltaForStore(UUID storeId, Instant since) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        Instant serverNow = Instant.now();

        List<Object[]> catMeta = em.createNativeQuery("SELECT name FROM catalog WHERE id = :id LIMIT 1")
                .setParameter("id", catalogId)
                .getResultList();
        String catalogName = (catMeta != null && !catMeta.isEmpty() && catMeta.get(0) != null)
                ? String.valueOf(catMeta.get(0))
                : "Cardápio Principal";

        Timestamp sinceTs = since != null ? Timestamp.from(since) : null;

        List<CategorySyncDto> categories = queryCategories(catalogId, sinceTs);
        List<ModifierGroupSyncDto> modifierGroups = queryModifierGroups(catalogId, sinceTs);
        List<AdditionalSyncDto> additionals = queryAdditionals(catalogId, sinceTs);
        List<IngredientSyncDto> ingredients = queryIngredients(catalogId, sinceTs);
        List<ProductSyncDto> products = queryProducts(catalogId, sinceTs);

        return new EdgeCatalogDeltaResponse(
                catalogId,
                catalogName,
                serverNow,
                categories,
                modifierGroups,
                additionals,
                ingredients,
                products
        );
    }

    @Override
    @Transactional
    public int applyDeltaLocally(UUID edgeStoreId, EdgeCatalogDeltaResponse delta) {
        if (edgeStoreId == null || delta == null || delta.catalogId() == null) {
            return 0;
        }

        ensureLocalCatalogAndLinkStore(edgeStoreId, delta.catalogId(), delta.catalogName());
        UUID catalogId = delta.catalogId();
        int applied = 0;

        if (delta.categories() != null) {
            for (CategorySyncDto c : delta.categories()) {
                applyCategory(catalogId, c);
                applied++;
            }
        }
        if (delta.ingredients() != null) {
            for (IngredientSyncDto i : delta.ingredients()) {
                applyIngredient(edgeStoreId, catalogId, i);
                applied++;
            }
        }
        if (delta.modifierGroups() != null) {
            for (ModifierGroupSyncDto g : delta.modifierGroups()) {
                applyModifierGroup(catalogId, g);
                applied++;
            }
        }
        if (delta.additionals() != null) {
            for (AdditionalSyncDto a : delta.additionals()) {
                applyAdditional(catalogId, a);
                applied++;
            }
        }
        if (delta.products() != null) {
            Map<String, UUID> localSectors = catalogSupport.loadStoreSectorsByCode(edgeStoreId);
            for (ProductSyncDto p : delta.products()) {
                applyProduct(edgeStoreId, catalogId, p, localSectors);
                applied++;
            }
        }

        return applied;
    }

    private void ensureLocalCatalogAndLinkStore(UUID edgeStoreId, UUID targetCatalogId, String catalogName) {
        List<?> tenantRows = em.createNativeQuery("SELECT tenant_id FROM store WHERE id = :id LIMIT 1")
                .setParameter("id", edgeStoreId)
                .getResultList();
        UUID tenantId = (tenantRows != null && !tenantRows.isEmpty() && tenantRows.get(0) != null)
                ? StoreCatalogContextSupport.toUuid(tenantRows.get(0))
                : DEFAULT_TENANT_ID;

        em.createNativeQuery("""
                INSERT INTO tenant_account (id, company_name, trade_name, owner_name, status, created_at, updated_at)
                VALUES (:tenantId, :name, :name, 'Administrador', 'ACTIVE', now(), now())
                ON CONFLICT (id) DO NOTHING
                """)
                .setParameter("tenantId", tenantId)
                .setParameter("name", catalogName != null ? catalogName : "Cliente Local")
                .executeUpdate();

        em.createNativeQuery("""
                INSERT INTO catalog (id, tenant_id, name, is_default, created_at, updated_at)
                VALUES (:id, :tenantId, :name, false, now(), now())
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    updated_at = now()
                """)
                .setParameter("id", targetCatalogId)
                .setParameter("tenantId", tenantId)
                .setParameter("name", catalogName != null ? catalogName : "Cardápio Principal")
                .executeUpdate();

        em.createNativeQuery("UPDATE store SET catalog_id = :catalogId WHERE id = :storeId AND catalog_id <> :catalogId")
                .setParameter("catalogId", targetCatalogId)
                .setParameter("storeId", edgeStoreId)
                .executeUpdate();
    }

    private void applyCategory(UUID catalogId, CategorySyncDto c) {
        String modCsv = (c.defaultModifierGroupIds() != null && !c.defaultModifierGroupIds().isEmpty())
                ? String.join(",", c.defaultModifierGroupIds().stream().map(UUID::toString).toList())
                : null;
        Timestamp updatedTs = Timestamp.from(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        Timestamp deletedTs = c.deletedAt() != null ? Timestamp.valueOf(c.deletedAt()) : null;

        em.createNativeQuery("""
                INSERT INTO product_category (
                    id, catalog_id, name, status, display_order, default_modifier_group_ids, created_at, updated_at, deleted_at
                ) VALUES (
                    :id, :catalogId, :name, :status, :displayOrder, :modCsv, :updatedAt, :updatedAt, :deletedAt
                )
                ON CONFLICT (id) DO UPDATE SET
                    catalog_id = EXCLUDED.catalog_id,
                    name = EXCLUDED.name,
                    status = EXCLUDED.status,
                    display_order = EXCLUDED.display_order,
                    default_modifier_group_ids = EXCLUDED.default_modifier_group_ids,
                    updated_at = EXCLUDED.updated_at,
                    deleted_at = EXCLUDED.deleted_at
                WHERE product_category.updated_at <= EXCLUDED.updated_at
                """)
                .setParameter("id", c.id())
                .setParameter("catalogId", catalogId)
                .setParameter("name", c.name())
                .setParameter("status", c.status() != null ? c.status() : "AVAILABLE")
                .setParameter("displayOrder", c.displayOrder())
                .setParameter("modCsv", modCsv)
                .setParameter("updatedAt", updatedTs)
                .setParameter("deletedAt", deletedTs)
                .executeUpdate();
    }

    private void applyIngredient(UUID edgeStoreId, UUID catalogId, IngredientSyncDto i) {
        Timestamp updatedTs = Timestamp.from(i.updatedAt() != null ? i.updatedAt() : Instant.now());
        Timestamp deletedTs = i.deletedAt() != null ? Timestamp.valueOf(i.deletedAt()) : null;

        em.createNativeQuery("""
                INSERT INTO ingredient (
                    id, catalog_id, name, unit, cost_price, active, created_at, updated_at, deleted_at
                ) VALUES (
                    :id, :catalogId, :name, :unit, :costPrice, :active, :updatedAt, :updatedAt, :deletedAt
                )
                ON CONFLICT (id) DO UPDATE SET
                    catalog_id = EXCLUDED.catalog_id,
                    name = EXCLUDED.name,
                    unit = EXCLUDED.unit,
                    cost_price = EXCLUDED.cost_price,
                    active = EXCLUDED.active,
                    updated_at = EXCLUDED.updated_at,
                    deleted_at = EXCLUDED.deleted_at
                WHERE ingredient.updated_at <= EXCLUDED.updated_at
                """)
                .setParameter("id", i.id())
                .setParameter("catalogId", catalogId)
                .setParameter("name", i.name())
                .setParameter("unit", i.unit() != null ? i.unit() : "UN")
                .setParameter("costPrice", i.costPrice() != null ? i.costPrice() : BigDecimal.ZERO)
                .setParameter("active", i.active())
                .setParameter("updatedAt", updatedTs)
                .setParameter("deletedAt", deletedTs)
                .executeUpdate();

        em.createNativeQuery("""
                INSERT INTO store_ingredient_stock (store_id, ingredient_id, stock, min_stock, updated_at)
                VALUES (:storeId, :ingredientId, 0, 0, now())
                ON CONFLICT (store_id, ingredient_id) DO NOTHING
                """)
                .setParameter("storeId", edgeStoreId)
                .setParameter("ingredientId", i.id())
                .executeUpdate();
    }

    private void applyModifierGroup(UUID catalogId, ModifierGroupSyncDto g) {
        Timestamp updatedTs = Timestamp.from(g.updatedAt() != null ? g.updatedAt() : Instant.now());
        Timestamp deletedTs = g.deletedAt() != null ? Timestamp.valueOf(g.deletedAt()) : null;

        int updatedRows = em.createNativeQuery("""
                INSERT INTO modifier_group (
                    id, catalog_id, name, type, semantic_type, required, min_selections, max_selections, icon,
                    created_at, updated_at, deleted_at
                ) VALUES (
                    :id, :catalogId, :name, :type, :semanticType, :required, :minSel, :maxSel, :icon,
                    :updatedAt, :updatedAt, :deletedAt
                )
                ON CONFLICT (id) DO UPDATE SET
                    catalog_id = EXCLUDED.catalog_id,
                    name = EXCLUDED.name,
                    type = EXCLUDED.type,
                    semantic_type = EXCLUDED.semantic_type,
                    required = EXCLUDED.required,
                    min_selections = EXCLUDED.min_selections,
                    max_selections = EXCLUDED.max_selections,
                    icon = EXCLUDED.icon,
                    updated_at = EXCLUDED.updated_at,
                    deleted_at = EXCLUDED.deleted_at
                WHERE modifier_group.updated_at <= EXCLUDED.updated_at
                """)
                .setParameter("id", g.id())
                .setParameter("catalogId", catalogId)
                .setParameter("name", g.name())
                .setParameter("type", g.type() != null ? g.type() : "RADIO")
                .setParameter("semanticType", g.semanticType())
                .setParameter("required", g.required())
                .setParameter("minSel", g.minSelections())
                .setParameter("maxSel", g.maxSelections())
                .setParameter("icon", g.icon())
                .setParameter("updatedAt", updatedTs)
                .setParameter("deletedAt", deletedTs)
                .executeUpdate();

        if (updatedRows > 0) {
            em.createNativeQuery("DELETE FROM modifier_option WHERE modifier_group_id = :groupId")
                    .setParameter("groupId", g.id())
                    .executeUpdate();
            if (g.options() != null) {
                for (ModifierOptionSyncDto opt : g.options()) {
                    em.createNativeQuery("""
                            INSERT INTO modifier_option (id, modifier_group_id, name, price, sort_order)
                            VALUES (:id, :groupId, :name, :price, :sortOrder)
                            ON CONFLICT (id) DO UPDATE SET
                                name = EXCLUDED.name,
                                price = EXCLUDED.price,
                                sort_order = EXCLUDED.sort_order
                            """)
                            .setParameter("id", opt.id() != null ? opt.id() : UUID.randomUUID())
                            .setParameter("groupId", g.id())
                            .setParameter("name", opt.name())
                            .setParameter("price", opt.price() != null ? opt.price() : BigDecimal.ZERO)
                            .setParameter("sortOrder", opt.sortOrder())
                            .executeUpdate();
                }
            }
        }
    }

    private void applyAdditional(UUID catalogId, AdditionalSyncDto a) {
        Timestamp updatedTs = Timestamp.from(a.updatedAt() != null ? a.updatedAt() : Instant.now());
        Timestamp deletedTs = a.deletedAt() != null ? Timestamp.valueOf(a.deletedAt()) : null;

        em.createNativeQuery("""
                INSERT INTO additional (
                    id, catalog_id, name, price, category, active, created_at, updated_at, deleted_at
                ) VALUES (
                    :id, :catalogId, :name, :price, :category, :active, :updatedAt, :updatedAt, :deletedAt
                )
                ON CONFLICT (id) DO UPDATE SET
                    catalog_id = EXCLUDED.catalog_id,
                    name = EXCLUDED.name,
                    price = EXCLUDED.price,
                    category = EXCLUDED.category,
                    active = EXCLUDED.active,
                    updated_at = EXCLUDED.updated_at,
                    deleted_at = EXCLUDED.deleted_at
                WHERE additional.updated_at <= EXCLUDED.updated_at
                """)
                .setParameter("id", a.id())
                .setParameter("catalogId", catalogId)
                .setParameter("name", a.name())
                .setParameter("price", a.price() != null ? a.price() : BigDecimal.ZERO)
                .setParameter("category", a.category())
                .setParameter("active", a.active())
                .setParameter("updatedAt", updatedTs)
                .setParameter("deletedAt", deletedTs)
                .executeUpdate();
    }

    private void applyProduct(UUID edgeStoreId, UUID catalogId, ProductSyncDto p, Map<String, UUID> localSectors) {
        Timestamp updatedTs = Timestamp.from(p.updatedAt() != null ? p.updatedAt() : Instant.now());
        Timestamp deletedTs = p.deletedAt() != null ? Timestamp.valueOf(p.deletedAt()) : null;
        String sectorCode = p.defaultSectorCode() != null ? p.defaultSectorCode() : "KITCHEN";

        int updatedRows = em.createNativeQuery("""
                INSERT INTO product (
                    id, version, catalog_id, name, category_id, description, price, estimated_cost, image_url,
                    status, external_code, catalog_item_type, product_type, track_stock, default_sector_code,
                    sale_unit, available_digital_menu, available_pdv, created_at, updated_at, deleted_at
                ) VALUES (
                    :id, 0, :catalogId, :name, :categoryId, :description, :price, :estimatedCost, :imageUrl,
                    :status, :externalCode, :catalogItemType, :productType, :trackStock, :defaultSectorCode,
                    :saleUnit, :availableDigital, :availablePdv, :updatedAt, :updatedAt, :deletedAt
                )
                ON CONFLICT (id) DO UPDATE SET
                    catalog_id = EXCLUDED.catalog_id,
                    name = EXCLUDED.name,
                    category_id = EXCLUDED.category_id,
                    description = EXCLUDED.description,
                    price = EXCLUDED.price,
                    estimated_cost = EXCLUDED.estimated_cost,
                    image_url = EXCLUDED.image_url,
                    status = EXCLUDED.status,
                    external_code = EXCLUDED.external_code,
                    catalog_item_type = EXCLUDED.catalog_item_type,
                    product_type = EXCLUDED.product_type,
                    track_stock = EXCLUDED.track_stock,
                    default_sector_code = EXCLUDED.default_sector_code,
                    sale_unit = EXCLUDED.sale_unit,
                    available_digital_menu = EXCLUDED.available_digital_menu,
                    available_pdv = EXCLUDED.available_pdv,
                    updated_at = EXCLUDED.updated_at,
                    deleted_at = EXCLUDED.deleted_at,
                    version = product.version + 1
                WHERE product.updated_at <= EXCLUDED.updated_at
                """)
                .setParameter("id", p.id())
                .setParameter("catalogId", catalogId)
                .setParameter("name", p.name())
                .setParameter("categoryId", p.categoryId())
                .setParameter("description", p.description())
                .setParameter("price", p.price() != null ? p.price() : BigDecimal.ZERO)
                .setParameter("estimatedCost", p.estimatedCost())
                .setParameter("imageUrl", p.imageUrl())
                .setParameter("status", p.status() != null ? p.status() : "AVAILABLE")
                .setParameter("externalCode", p.externalCode())
                .setParameter("catalogItemType", p.catalogItemType() != null ? p.catalogItemType() : "DEFAULT")
                .setParameter("productType", p.productType() != null ? p.productType() : "PRODUCED")
                .setParameter("trackStock", p.trackStock())
                .setParameter("defaultSectorCode", sectorCode)
                .setParameter("saleUnit", p.saleUnit() != null ? p.saleUnit() : "UN")
                .setParameter("availableDigital", p.availableDigitalMenu())
                .setParameter("availablePdv", p.availablePdv())
                .setParameter("updatedAt", updatedTs)
                .setParameter("deletedAt", deletedTs)
                .executeUpdate();

        UUID defaultLocalSectorId = localSectors.get(sectorCode.toUpperCase());
        em.createNativeQuery("""
                INSERT INTO store_product_setting (store_id, product_id, stock, local_status, production_sector_id, updated_at)
                VALUES (:storeId, :productId, 0, 'AVAILABLE', :sectorId, now())
                ON CONFLICT (store_id, product_id) DO NOTHING
                """)
                .setParameter("storeId", edgeStoreId)
                .setParameter("productId", p.id())
                .setParameter("sectorId", defaultLocalSectorId)
                .executeUpdate();

        if (updatedRows > 0) {
            em.createNativeQuery("DELETE FROM product_channel_price WHERE product_id = :productId")
                    .setParameter("productId", p.id()).executeUpdate();
            if (p.channelPrices() != null) {
                for (ChannelPriceSyncDto cp : p.channelPrices()) {
                    em.createNativeQuery("""
                            INSERT INTO product_channel_price (product_id, channel, price)
                            VALUES (:productId, :channel, :price)
                            ON CONFLICT (product_id, channel) DO UPDATE SET price = EXCLUDED.price
                            """)
                            .setParameter("productId", p.id())
                            .setParameter("channel", cp.channel())
                            .setParameter("price", cp.price() != null ? cp.price() : BigDecimal.ZERO)
                            .executeUpdate();
                }
            }

            em.createNativeQuery("DELETE FROM technical_sheet_item WHERE product_id = :productId")
                    .setParameter("productId", p.id()).executeUpdate();
            if (p.technicalSheet() != null) {
                for (TechnicalSheetSyncDto ts : p.technicalSheet()) {
                    em.createNativeQuery("""
                            INSERT INTO technical_sheet_item (product_id, ingredient_id, quantity, cost_snapshot)
                            VALUES (:productId, :ingId, :qty, :cost)
                            ON CONFLICT (product_id, ingredient_id) DO UPDATE SET
                                quantity = EXCLUDED.quantity,
                                cost_snapshot = EXCLUDED.cost_snapshot
                            """)
                            .setParameter("productId", p.id())
                            .setParameter("ingId", ts.ingredientId())
                            .setParameter("qty", ts.quantity() != null ? ts.quantity() : BigDecimal.ONE)
                            .setParameter("cost", ts.costSnapshot() != null ? ts.costSnapshot() : BigDecimal.ZERO)
                            .executeUpdate();
                }
            }

            em.createNativeQuery("DELETE FROM product_removable_item WHERE product_id = :productId")
                    .setParameter("productId", p.id()).executeUpdate();
            if (p.removableItemIds() != null) {
                for (UUID remId : p.removableItemIds()) {
                    em.createNativeQuery("""
                            INSERT INTO product_removable_item (product_id, ingredient_id)
                            VALUES (:productId, :ingId)
                            ON CONFLICT DO NOTHING
                            """)
                            .setParameter("productId", p.id())
                            .setParameter("ingId", remId)
                            .executeUpdate();
                }
            }

            em.createNativeQuery("DELETE FROM product_modifier_group WHERE product_id = :productId")
                    .setParameter("productId", p.id()).executeUpdate();
            if (p.modifierGroupIds() != null) {
                for (UUID modId : p.modifierGroupIds()) {
                    em.createNativeQuery("""
                            INSERT INTO product_modifier_group (product_id, modifier_group_id)
                            VALUES (:productId, :groupId)
                            ON CONFLICT DO NOTHING
                            """)
                            .setParameter("productId", p.id())
                            .setParameter("groupId", modId)
                            .executeUpdate();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<CategorySyncDto> queryCategories(UUID catalogId, Timestamp sinceTs) {
        String sql = sinceTs == null
                ? "SELECT id, name, status, display_order, default_modifier_group_ids, updated_at, deleted_at FROM product_category WHERE catalog_id = :catalogId AND deleted_at IS NULL ORDER BY display_order ASC"
                : "SELECT id, name, status, display_order, default_modifier_group_ids, updated_at, deleted_at FROM product_category WHERE catalog_id = :catalogId AND updated_at > :since ORDER BY updated_at ASC";
        var q = em.createNativeQuery(sql).setParameter("catalogId", catalogId);
        if (sinceTs != null) q.setParameter("since", sinceTs);

        List<Object[]> rows = q.getResultList();
        List<CategorySyncDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            List<UUID> modIds = new ArrayList<>();
            if (r[4] != null && !r[4].toString().isBlank()) {
                for (String part : r[4].toString().split(",")) {
                    if (!part.isBlank()) modIds.add(UUID.fromString(part.trim()));
                }
            }
            out.add(new CategorySyncDto(
                    StoreCatalogContextSupport.toUuid(r[0]),
                    str(r[1]),
                    str(r[2]),
                    numInt(r[3]),
                    modIds,
                    toInstant(r[5]),
                    toLocalDateTime(r[6])
            ));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<ModifierGroupSyncDto> queryModifierGroups(UUID catalogId, Timestamp sinceTs) {
        String sql = sinceTs == null
                ? "SELECT id, name, type, semantic_type, required, min_selections, max_selections, icon, updated_at, deleted_at FROM modifier_group WHERE catalog_id = :catalogId AND deleted_at IS NULL"
                : "SELECT id, name, type, semantic_type, required, min_selections, max_selections, icon, updated_at, deleted_at FROM modifier_group WHERE catalog_id = :catalogId AND updated_at > :since";
        var q = em.createNativeQuery(sql).setParameter("catalogId", catalogId);
        if (sinceTs != null) q.setParameter("since", sinceTs);

        List<Object[]> rows = q.getResultList();
        List<ModifierGroupSyncDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            UUID groupId = StoreCatalogContextSupport.toUuid(r[0]);
            List<Object[]> optRows = em.createNativeQuery(
                            "SELECT id, name, price, sort_order, linked_product_id FROM modifier_option WHERE modifier_group_id = :gid ORDER BY sort_order ASC")
                    .setParameter("gid", groupId)
                    .getResultList();
            List<ModifierOptionSyncDto> opts = optRows.stream()
                    .map(or -> new ModifierOptionSyncDto(
                            StoreCatalogContextSupport.toUuid(or[0]),
                            str(or[1]),
                            toDecimal(or[2]),
                            numInt(or[3]),
                            or[4] != null ? StoreCatalogContextSupport.toUuid(or[4]) : null
                    ))
                    .toList();
            out.add(new ModifierGroupSyncDto(
                    groupId,
                    str(r[1]),
                    str(r[2]),
                    str(r[3]),
                    bool(r[4]),
                    numInt(r[5]),
                    numInt(r[6]),
                    str(r[7]),
                    opts,
                    toInstant(r[8]),
                    toLocalDateTime(r[9])
            ));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<AdditionalSyncDto> queryAdditionals(UUID catalogId, Timestamp sinceTs) {
        String sql = sinceTs == null
                ? "SELECT id, name, price, category, active, updated_at, deleted_at FROM additional WHERE catalog_id = :catalogId AND deleted_at IS NULL"
                : "SELECT id, name, price, category, active, updated_at, deleted_at FROM additional WHERE catalog_id = :catalogId AND updated_at > :since";
        var q = em.createNativeQuery(sql).setParameter("catalogId", catalogId);
        if (sinceTs != null) q.setParameter("since", sinceTs);

        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new AdditionalSyncDto(
                StoreCatalogContextSupport.toUuid(r[0]),
                str(r[1]),
                toDecimal(r[2]),
                str(r[3]),
                bool(r[4]),
                toInstant(r[5]),
                toLocalDateTime(r[6])
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private List<IngredientSyncDto> queryIngredients(UUID catalogId, Timestamp sinceTs) {
        String sql = sinceTs == null
                ? "SELECT id, name, unit, cost_price, active, updated_at, deleted_at FROM ingredient WHERE catalog_id = :catalogId AND deleted_at IS NULL"
                : "SELECT id, name, unit, cost_price, active, updated_at, deleted_at FROM ingredient WHERE catalog_id = :catalogId AND updated_at > :since";
        var q = em.createNativeQuery(sql).setParameter("catalogId", catalogId);
        if (sinceTs != null) q.setParameter("since", sinceTs);

        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new IngredientSyncDto(
                StoreCatalogContextSupport.toUuid(r[0]),
                str(r[1]),
                str(r[2]),
                toDecimal(r[3]),
                bool(r[4]),
                toInstant(r[5]),
                toLocalDateTime(r[6])
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private List<ProductSyncDto> queryProducts(UUID catalogId, Timestamp sinceTs) {
        String sql = sinceTs == null
                ? """
                  SELECT id, name, category_id, description, price, estimated_cost, image_url, status,
                         external_code, catalog_item_type, product_type, track_stock, default_sector_code,
                         sale_unit, available_digital_menu, available_pdv, updated_at, deleted_at
                  FROM product
                  WHERE catalog_id = :catalogId AND deleted_at IS NULL
                  """
                : """
                  SELECT id, name, category_id, description, price, estimated_cost, image_url, status,
                         external_code, catalog_item_type, product_type, track_stock, default_sector_code,
                         sale_unit, available_digital_menu, available_pdv, updated_at, deleted_at
                  FROM product
                  WHERE catalog_id = :catalogId AND updated_at > :since
                  """;
        var q = em.createNativeQuery(sql).setParameter("catalogId", catalogId);
        if (sinceTs != null) q.setParameter("since", sinceTs);

        List<Object[]> rows = q.getResultList();
        List<ProductSyncDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            UUID prodId = StoreCatalogContextSupport.toUuid(r[0]);

            List<Object[]> cpRows = em.createNativeQuery("SELECT channel, price FROM product_channel_price WHERE product_id = :pid")
                    .setParameter("pid", prodId).getResultList();
            List<ChannelPriceSyncDto> cps = cpRows.stream()
                    .map(cr -> new ChannelPriceSyncDto(str(cr[0]), toDecimal(cr[1]))).toList();

            List<Object[]> tsRows = em.createNativeQuery("SELECT ingredient_id, quantity, cost_snapshot FROM technical_sheet_item WHERE product_id = :pid")
                    .setParameter("pid", prodId).getResultList();
            List<TechnicalSheetSyncDto> sheet = tsRows.stream()
                    .map(tr -> new TechnicalSheetSyncDto(StoreCatalogContextSupport.toUuid(tr[0]), toDecimal(tr[1]), toDecimal(tr[2]))).toList();

            List<?> remRows = em.createNativeQuery("SELECT ingredient_id FROM product_removable_item WHERE product_id = :pid")
                    .setParameter("pid", prodId).getResultList();
            List<UUID> rems = remRows.stream().map(StoreCatalogContextSupport::toUuid).toList();

            List<?> modRows = em.createNativeQuery("SELECT modifier_group_id FROM product_modifier_group WHERE product_id = :pid")
                    .setParameter("pid", prodId).getResultList();
            List<UUID> mods = modRows.stream().map(StoreCatalogContextSupport::toUuid).toList();

            out.add(new ProductSyncDto(
                    prodId,
                    str(r[1]),
                    r[2] != null ? StoreCatalogContextSupport.toUuid(r[2]) : null,
                    str(r[3]),
                    toDecimal(r[4]),
                    r[5] != null ? toDecimal(r[5]) : null,
                    str(r[6]),
                    str(r[7]),
                    str(r[8]),
                    str(r[9]),
                    str(r[10]),
                    bool(r[11]),
                    str(r[12]),
                    str(r[13]),
                    bool(r[14]),
                    bool(r[15]),
                    cps,
                    sheet,
                    rems,
                    mods,
                    toInstant(r[16]),
                    toLocalDateTime(r[17])
            ));
        }
        return out;
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static int numInt(Object o) {
        return o instanceof Number n ? n.intValue() : 0;
    }

    private static boolean bool(Object o) {
        return o instanceof Boolean b && b;
    }

    private static BigDecimal toDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        return new BigDecimal(o.toString());
    }

    private static Instant toInstant(Object o) {
        if (o == null) return Instant.now();
        if (o instanceof Timestamp ts) return ts.toInstant();
        if (o instanceof Instant inst) return inst;
        return Instant.parse(o.toString());
    }

    private static LocalDateTime toLocalDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof Timestamp ts) return ts.toLocalDateTime();
        if (o instanceof LocalDateTime ldt) return ldt;
        return LocalDateTime.parse(o.toString());
    }
}

