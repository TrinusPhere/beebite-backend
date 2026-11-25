package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.port.EdgeStoreBootstrapPort;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * Adaptador JPA/PostgreSQL responsável por provisionar a loja real do cliente no banco local Edge,
 * migrar os setores de produção padrão, garantir o usuário local como {@code ADMIN} (nunca {@code SUPER_ADMIN})
 * e remover completamente os dados seed da Trinusphere ({@code 00000000-0000-0000-0000-000000000001}).
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
            String storeName,
            String publicSlug,
            String address,
            String adminLogin,
            UUID defaultSeedStoreId) {

        Number existsCount = (Number) em.createNativeQuery("SELECT COUNT(*) FROM store WHERE id = :id")
                .setParameter("id", edgeStoreId)
                .getSingleResult();

        if (existsCount.longValue() == 0L) {
            em.createNativeQuery("""
                    INSERT INTO store (id, name, is_open, active, address, public_slug, sync_mode, created_at, updated_at)
                    VALUES (:id, :name, true, true, :address, :slug, 'EDGE', now(), now())
                    ON CONFLICT (id) DO NOTHING
                    """)
                    .setParameter("id", edgeStoreId)
                    .setParameter("name", storeName)
                    .setParameter("address", address)
                    .setParameter("slug", publicSlug)
                    .executeUpdate();
        } else {
            em.createNativeQuery("""
                    UPDATE store
                    SET active = true,
                        deleted_at = NULL,
                        sync_mode = 'EDGE',
                        updated_at = now()
                    WHERE id = :id
                    """)
                    .setParameter("id", edgeStoreId)
                    .executeUpdate();
        }

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
                              (SELECT COUNT(*) FROM product WHERE store_id = :seedStoreId) +
                              (SELECT COUNT(*) FROM ingredient WHERE store_id = :seedStoreId) +
                              (SELECT COUNT(*) FROM customer WHERE store_id = :seedStoreId)
                            """)
                    .setParameter("seedStoreId", defaultSeedStoreId)
                    .getSingleResult();
            if (seedReferences.longValue() == 0L) {
                int removedStores = em.createNativeQuery("DELETE FROM store WHERE id = :seedStoreId")
                        .setParameter("seedStoreId", defaultSeedStoreId)
                        .executeUpdate();
                em.createNativeQuery("""
                        DELETE FROM tenant_account
                        WHERE id = :seedStoreId
                          AND NOT EXISTS (SELECT 1 FROM store WHERE tenant_id = :seedStoreId)
                        """)
                        .setParameter("seedStoreId", defaultSeedStoreId)
                        .executeUpdate();
                if (removedStores > 0) {
                    LOG.info("[EDGE-BOOTSTRAP] Loja e tenant seed padrão removidos do banco local Edge.");
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
