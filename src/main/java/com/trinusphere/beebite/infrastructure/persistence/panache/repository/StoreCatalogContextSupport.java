package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CatalogEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolve o {@code catalogId} associado a uma {@code storeId} e auxilia na resolução de setores de produção por código
 * (ex.: {@code KITCHEN} da Loja A ↔ {@code KITCHEN} da Loja B).
 */
@ApplicationScoped
public class StoreCatalogContextSupport {

    public static final UUID FOREIGN_CATALOG_SENTINEL = new UUID(0L, 0xDEAD_C0DEL);
    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    EntityManager em;

    public UUID resolveCatalogIdForStore(UUID storeOrCatalogId) {
        if (storeOrCatalogId == null) {
            return null;
        }
        List<?> storeRows = em.createNativeQuery("SELECT catalog_id FROM store WHERE id = :id LIMIT 1")
                .setParameter("id", storeOrCatalogId)
                .getResultList();
        if (storeRows != null && !storeRows.isEmpty() && storeRows.get(0) != null) {
            return toUuid(storeRows.get(0));
        }

        // Caso já seja um catalog_id direto
        List<?> catRows = em.createNativeQuery("SELECT id FROM catalog WHERE id = :id LIMIT 1")
                .setParameter("id", storeOrCatalogId)
                .getResultList();
        if (catRows != null && !catRows.isEmpty() && catRows.get(0) != null) {
            return toUuid(catRows.get(0));
        }

        return storeOrCatalogId;
    }

    @Transactional
    public UUID ensureCatalogIdForNewStore(UUID tenantId, String storeName, UUID explicitCatalogId) {
        if (explicitCatalogId != null) {
            return explicitCatalogId;
        }
        UUID effectiveTenantId = tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        List<?> existingDefault = em.createNativeQuery("""
                        SELECT id FROM catalog
                        WHERE tenant_id = :tenantId AND deleted_at IS NULL
                        ORDER BY is_default DESC, created_at ASC
                        LIMIT 1
                        """)
                .setParameter("tenantId", effectiveTenantId)
                .getResultList();
        if (existingDefault != null && !existingDefault.isEmpty() && existingDefault.get(0) != null) {
            return toUuid(existingDefault.get(0));
        }

        CatalogEntity created = new CatalogEntity();
        created.tenantId = effectiveTenantId;
        created.name = "Cardápio Principal - " + (storeName != null ? storeName : "Loja");
        created.defaultCatalog = true;
        em.persist(created);
        em.flush();
        return created.id;
    }

    public boolean isStoreIdKnown(UUID candidateStoreId) {
        if (candidateStoreId == null) {
            return false;
        }
        List<?> rows = em.createNativeQuery("SELECT 1 FROM store WHERE id = :id LIMIT 1")
                .setParameter("id", candidateStoreId)
                .getResultList();
        return rows != null && !rows.isEmpty();
    }

    public UUID resolveRepresentativeStoreIdForCatalog(UUID catalogId, UUID preferredStoreId) {
        if (preferredStoreId != null && isStoreIdKnown(preferredStoreId)) {
            return preferredStoreId;
        }
        if (catalogId == null) {
            return null;
        }
        List<?> rows = em.createNativeQuery("""
                        SELECT id FROM store
                        WHERE catalog_id = :catalogId AND deleted_at IS NULL
                        ORDER BY CASE WHEN sync_mode = 'EDGE' THEN 0 ELSE 1 END, created_at ASC
                        LIMIT 1
                        """)
                .setParameter("catalogId", catalogId)
                .getResultList();
        if (rows != null && !rows.isEmpty() && rows.get(0) != null) {
            return toUuid(rows.get(0));
        }
        return null;
    }

    public String resolveSectorCode(UUID productionSectorId) {
        if (productionSectorId == null) {
            return "KITCHEN";
        }
        List<?> rows = em.createNativeQuery("SELECT code FROM production_sector WHERE id = :id LIMIT 1")
                .setParameter("id", productionSectorId)
                .getResultList();
        if (rows != null && !rows.isEmpty() && rows.get(0) != null && !rows.get(0).toString().isBlank()) {
            return rows.get(0).toString().trim().toUpperCase();
        }
        return "KITCHEN";
    }

    @SuppressWarnings("unchecked")
    public Map<String, UUID> loadStoreSectorsByCode(UUID storeId) {
        Map<String, UUID> map = new HashMap<>();
        if (storeId == null) {
            return map;
        }
        List<Object[]> rows = em.createNativeQuery("""
                        SELECT upper(code), id
                        FROM production_sector
                        WHERE store_id = :storeId AND deleted_at IS NULL AND code IS NOT NULL
                        """)
                .setParameter("storeId", storeId)
                .getResultList();
        for (Object[] row : rows) {
            if (row[0] != null && row[1] != null) {
                map.putIfAbsent(row[0].toString(), toUuid(row[1]));
            }
        }
        return map;
    }

    public static UUID toUuid(Object val) {
        if (val instanceof UUID u) {
            return u;
        }
        return UUID.fromString(val.toString());
    }
}
