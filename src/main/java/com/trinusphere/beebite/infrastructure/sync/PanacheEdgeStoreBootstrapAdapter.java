package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.port.EdgeStoreBootstrapPort;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador JPA/PostgreSQL responsável por provisionar a loja real do cliente e o catálogo
 * compartilhado no banco local Edge, migrar os setores de produção padrão, garantir o usuário local
 * como {@code ADMIN} (nunca {@code SUPER_ADMIN}) e remover os dados seed da Trinusphere
 * ({@code 00000000-0000-0000-0000-000000000001}).
 */
@ApplicationScoped
public class PanacheEdgeStoreBootstrapAdapter implements EdgeStoreBootstrapPort {

    private static final Logger LOG = Logger.getLogger(PanacheEdgeStoreBootstrapAdapter.class);

    @Inject
    EntityManager em;

    @Override
    @Transactional
    public void bootstrapClientStoreAndCleanSeed(
            UUID edgeStoreId,
            UUID edgeCatalogId,
            String storeName,
            String publicSlug,
            String address,
            String adminLogin,
            UUID defaultSeedStoreId) {

        UUID targetCatalogId = edgeCatalogId != null ? edgeCatalogId : edgeStoreId;

        // 1. Garantir TenantAccount local da loja Edge
        em.createNativeQuery("""
                INSERT INTO tenant_account (id, company_name, trade_name, owner_name, status, created_at, updated_at)
                VALUES (:tenantId, :storeName, :storeName, :ownerName, 'ACTIVE', now(), now())
                ON CONFLICT (id) DO NOTHING
                """)
                .setParameter("tenantId", edgeStoreId)
                .setParameter("storeName", storeName)
                .setParameter("ownerName", adminLogin != null && !adminLogin.isBlank() ? adminLogin : "admin")
                .executeUpdate();

        // 2. Garantir Catalog local com o mesmo UUID compartilhado da Cloud
        em.createNativeQuery("""
                INSERT INTO catalog (id, tenant_id, name, is_default, created_at, updated_at)
                VALUES (
                    :catalogId,
                    :tenantId,
                    :catalogName,
                    NOT EXISTS (SELECT 1 FROM catalog WHERE tenant_id = :tenantId AND is_default = true AND deleted_at IS NULL),
                    now(),
                    now()
                )
                ON CONFLICT (id) DO NOTHING
                """)
                .setParameter("catalogId", targetCatalogId)
                .setParameter("tenantId", edgeStoreId)
                .setParameter("catalogName", "Cardápio Principal - " + storeName)
                .executeUpdate();

        // 3. Provisionar ou atualizar a loja Edge
        @SuppressWarnings("unchecked")
        List<Object> existingCatalogRows = em.createNativeQuery("SELECT catalog_id FROM store WHERE id = :id")
                .setParameter("id", edgeStoreId)
                .getResultList();

        if (existingCatalogRows.isEmpty()) {
            em.createNativeQuery("""
                    INSERT INTO store (id, tenant_id, catalog_id, name, is_open, active, address, public_slug, sync_mode, created_at, updated_at)
                    VALUES (:id, :tenantId, :catalogId, :name, true, true, :address, :slug, 'EDGE', now(), now())
                    ON CONFLICT (id) DO NOTHING
                    """)
                    .setParameter("id", edgeStoreId)
                    .setParameter("tenantId", edgeStoreId)
                    .setParameter("catalogId", targetCatalogId)
                    .setParameter("name", storeName)
                    .setParameter("address", address)
                    .setParameter("slug", publicSlug)
                    .executeUpdate();
        } else {
            Object rawExistingCatalog = existingCatalogRows.get(0);
            UUID previousCatalogId = rawExistingCatalog != null ? UUID.fromString(rawExistingCatalog.toString()) : null;

            if (previousCatalogId != null && !previousCatalogId.equals(targetCatalogId)) {
                migrateLocalCatalogRecords(previousCatalogId, targetCatalogId);
                em.createNativeQuery("""
                        UPDATE store
                        SET active = true,
                            deleted_at = NULL,
                            sync_mode = 'EDGE',
                            catalog_id = :catalogId,
                            last_catalog_pull_at = NULL,
                            updated_at = now()
                        WHERE id = :id
                        """)
                        .setParameter("catalogId", targetCatalogId)
                        .setParameter("id", edgeStoreId)
                        .executeUpdate();
            } else {
                em.createNativeQuery("""
                        UPDATE store
                        SET active = true,
                            deleted_at = NULL,
                            sync_mode = 'EDGE',
                            catalog_id = :catalogId,
                            updated_at = now()
                        WHERE id = :id
                        """)
                        .setParameter("catalogId", targetCatalogId)
                        .setParameter("id", edgeStoreId)
                        .executeUpdate();
            }
        }

        // 4. Migrar setores de produção / usuários e limpar dados seed da Trinusphere
        if (!edgeStoreId.equals(defaultSeedStoreId)) {
            Number targetSectors = (Number) em.createNativeQuery(
                            "SELECT COUNT(*) FROM production_sector WHERE store_id = :id AND deleted_at IS NULL")
                    .setParameter("id", edgeStoreId)
                    .getSingleResult();
            if (targetSectors.longValue() == 0L) {
                em.createNativeQuery("""
                        UPDATE production_sector
                        SET store_id = :edgeStoreId, updated_at = now()
                        WHERE store_id = :seedStoreId
                        """)
                        .setParameter("edgeStoreId", edgeStoreId)
                        .setParameter("seedStoreId", defaultSeedStoreId)
                        .executeUpdate();
            }

            em.createNativeQuery("""
                    UPDATE users
                    SET store_id = :edgeStoreId, updated_at = now()
                    WHERE store_id = :seedStoreId
                    """)
                    .setParameter("edgeStoreId", edgeStoreId)
                    .setParameter("seedStoreId", defaultSeedStoreId)
                    .executeUpdate();

            em.createNativeQuery("DELETE FROM user_store_access WHERE store_id = :seedStoreId")
                    .setParameter("seedStoreId", defaultSeedStoreId)
                    .executeUpdate();

            Number seedReferences = (Number) em.createNativeQuery("""
                            SELECT
                              (SELECT COUNT(*) FROM orders WHERE store_id = :seedStoreId) +
                              (SELECT COUNT(*) FROM product WHERE catalog_id = :seedStoreId) +
                              (SELECT COUNT(*) FROM ingredient WHERE catalog_id = :seedStoreId) +
                              (SELECT COUNT(*) FROM customer WHERE store_id = :seedStoreId)
                            """)
                    .setParameter("seedStoreId", defaultSeedStoreId)
                    .getSingleResult();
            if (seedReferences.longValue() == 0L) {
                int removedStores = em.createNativeQuery("DELETE FROM store WHERE id = :seedStoreId")
                        .setParameter("seedStoreId", defaultSeedStoreId)
                        .executeUpdate();
                em.createNativeQuery("""
                        DELETE FROM catalog
                        WHERE id = :seedStoreId
                          AND NOT EXISTS (SELECT 1 FROM store WHERE catalog_id = :seedStoreId)
                        """)
                        .setParameter("seedStoreId", defaultSeedStoreId)
                        .executeUpdate();
                em.createNativeQuery("""
                        DELETE FROM tenant_account
                        WHERE id = :seedStoreId
                          AND NOT EXISTS (SELECT 1 FROM store WHERE tenant_id = :seedStoreId)
                          AND NOT EXISTS (SELECT 1 FROM catalog WHERE tenant_id = :seedStoreId)
                        """)
                        .setParameter("seedStoreId", defaultSeedStoreId)
                        .executeUpdate();
                if (removedStores > 0) {
                    LOG.info("[EDGE-BOOTSTRAP] Loja, catálogo e tenant seed padrão removidos do banco local Edge.");
                }
            } else {
                em.createNativeQuery("""
                        UPDATE store
                        SET active = false, deleted_at = now(), updated_at = now()
                        WHERE id = :seedStoreId
                        """)
                        .setParameter("seedStoreId", defaultSeedStoreId)
                        .executeUpdate();
            }
        }
    }

    private void migrateLocalCatalogRecords(UUID fromCatalogId, UUID toCatalogId) {
        for (String table : List.of("product_category", "modifier_group", "additional", "ingredient", "product")) {
            em.createNativeQuery("UPDATE " + table + " SET catalog_id = :toId, updated_at = now() WHERE catalog_id = :fromId")
                    .setParameter("toId", toCatalogId)
                    .setParameter("fromId", fromCatalogId)
                    .executeUpdate();
        }
        LOG.infof("[EDGE-BOOTSTRAP] Registros locais de catálogo migrados de %s para %s.", fromCatalogId, toCatalogId);
    }

    @Override
    @Transactional
    public void ensureLocalAdminIsStoreAdmin(UUID edgeStoreId, String adminLogin, String adminDisplayName) {
        String allPermissionsCsv = String.join(",", Permission.allValues());

        if (adminLogin != null && !adminLogin.isBlank() && !"admin".equalsIgnoreCase(adminLogin)) {
            Number targetLoginExists = (Number) em.createNativeQuery(
                            "SELECT COUNT(*) FROM users WHERE lower(login) = lower(:login) AND deleted_at IS NULL")
                    .setParameter("login", adminLogin)
                    .getSingleResult();
            if (targetLoginExists.longValue() == 0L) {
                em.createNativeQuery("""
                        UPDATE users
                        SET login = :newLogin, updated_at = now()
                        WHERE lower(login) = 'admin' AND deleted_at IS NULL
                        """)
                        .setParameter("newLogin", adminLogin)
                        .executeUpdate();
            }
        }

        em.createNativeQuery("""
                UPDATE users
                SET store_id = :edgeStoreId,
                    role = 'ADMIN',
                    name = CASE
                        WHEN name IN ('Trinusphere Super Admin', 'Administrador') THEN :displayName
                        ELSE name
                    END,
                    permissions = CAST(string_to_array(:permsCsv, ',') AS text[]),
                    updated_at = now()
                WHERE (role = 'SUPER_ADMIN' OR lower(login) = lower(:adminLogin))
                  AND deleted_at IS NULL
                """)
                .setParameter("edgeStoreId", edgeStoreId)
                .setParameter("displayName", adminDisplayName)
                .setParameter("permsCsv", allPermissionsCsv)
                .setParameter("adminLogin", adminLogin != null ? adminLogin : "admin")
                .executeUpdate();

        em.createNativeQuery("""
                INSERT INTO user_store_access (user_id, store_id, role, permissions)
                SELECT id, :edgeStoreId, role, permissions
                FROM users
                WHERE deleted_at IS NULL
                ON CONFLICT (user_id, store_id) DO UPDATE
                    SET role = EXCLUDED.role,
                        permissions = EXCLUDED.permissions
                """)
                .setParameter("edgeStoreId", edgeStoreId)
                .executeUpdate();
    }
}
