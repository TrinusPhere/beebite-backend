package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.port.EdgeStoreBootstrapPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Inicializa automaticamente o banco local de uma unidade {@code EDGE} com a loja real do cliente,
 * associa seu {@code catalog_id} compartilhado, remove a loja/tenant seed padrão
 * ({@code 00000000-0000-0000-0000-000000000001}) e garante que o usuário inicial local seja
 * {@code ADMIN} da loja (jamais {@code SUPER_ADMIN}).
 */
@ApplicationScoped
public class BootstrapEdgeStoreUseCase {

    private static final Logger LOG = Logger.getLogger(BootstrapEdgeStoreUseCase.class);
    static final UUID DEFAULT_SEED_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final EdgeStoreBootstrapPort bootstrapPort;
    private final String syncMode;
    private final Optional<String> edgeStoreIdRaw;
    private final Optional<String> edgeCatalogIdRaw;
    private final Optional<String> edgeStoreName;
    private final Optional<String> edgeStoreSlug;
    private final Optional<String> edgeStoreAddress;
    private final Optional<String> edgeAdminLogin;

    @Inject
    public BootstrapEdgeStoreUseCase(
            EdgeStoreBootstrapPort bootstrapPort,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.edge.store-id") Optional<String> edgeStoreIdRaw,
            @ConfigProperty(name = "biteflow.edge.catalog-id") Optional<String> edgeCatalogIdRaw,
            @ConfigProperty(name = "biteflow.edge.store-name") Optional<String> edgeStoreName,
            @ConfigProperty(name = "biteflow.edge.store-slug") Optional<String> edgeStoreSlug,
            @ConfigProperty(name = "biteflow.edge.store-address") Optional<String> edgeStoreAddress,
            @ConfigProperty(name = "biteflow.edge.admin-login") Optional<String> edgeAdminLogin) {
        this.bootstrapPort = bootstrapPort;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase(Locale.ROOT) : "CLOUD";
        this.edgeStoreIdRaw = edgeStoreIdRaw != null ? edgeStoreIdRaw : Optional.empty();
        this.edgeCatalogIdRaw = edgeCatalogIdRaw != null ? edgeCatalogIdRaw : Optional.empty();
        this.edgeStoreName = edgeStoreName != null ? edgeStoreName : Optional.empty();
        this.edgeStoreSlug = edgeStoreSlug != null ? edgeStoreSlug : Optional.empty();
        this.edgeStoreAddress = edgeStoreAddress != null ? edgeStoreAddress : Optional.empty();
        this.edgeAdminLogin = edgeAdminLogin != null ? edgeAdminLogin : Optional.empty();
    }

    public boolean execute() {
        if (!"EDGE".equals(syncMode)) {
            return false;
        }

        String rawId = edgeStoreIdRaw.map(String::trim).orElse("");
        if (rawId.isBlank()) {
            LOG.warn("[EDGE-BOOTSTRAP] Modo EDGE ativo, mas BEEBITE_EDGE_STORE_ID não foi informado; pulando provisionamento automático da loja.");
            return false;
        }

        UUID edgeStoreId;
        try {
            edgeStoreId = UUID.fromString(rawId);
        } catch (IllegalArgumentException ex) {
            LOG.errorf("[EDGE-BOOTSTRAP] BEEBITE_EDGE_STORE_ID inválido (esperado UUID): '%s'", rawId);
            return false;
        }

        UUID resolvedCatalogId = resolveCatalogId(edgeStoreId);
        String resolvedName = edgeStoreName.map(String::trim).filter(s -> !s.isBlank()).orElse("Unidade Balcão");
        String resolvedSlug = edgeStoreSlug.map(String::trim).filter(s -> !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .orElseGet(() -> slugify(resolvedName));
        String resolvedAddress = edgeStoreAddress.map(String::trim).filter(s -> !s.isBlank())
                .orElse("Unidade Presencial / Balcão");
        String resolvedAdminLogin = edgeAdminLogin.map(String::trim).filter(s -> !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .orElse("admin");

        bootstrapPort.bootstrapClientStoreAndCleanSeed(
                edgeStoreId,
                resolvedCatalogId,
                resolvedName,
                resolvedSlug,
                resolvedAddress,
                resolvedAdminLogin,
                DEFAULT_SEED_STORE_ID
        );

        bootstrapPort.ensureLocalAdminIsStoreAdmin(
                edgeStoreId,
                resolvedAdminLogin,
                "Administrador - " + resolvedName
        );

        LOG.infof("[EDGE-BOOTSTRAP] Unidade Edge '%s' (store=%s, catalog=%s) inicializada com sucesso (admin=%s).",
                resolvedName, edgeStoreId, resolvedCatalogId, resolvedAdminLogin);
        return true;
    }

    private UUID resolveCatalogId(UUID fallbackStoreId) {
        String rawCatalog = edgeCatalogIdRaw.map(String::trim).orElse("");
        if (rawCatalog.isBlank()) {
            return fallbackStoreId;
        }
        try {
            return UUID.fromString(rawCatalog);
        } catch (IllegalArgumentException ex) {
            LOG.warnf("[EDGE-BOOTSTRAP] BEEBITE_EDGE_CATALOG_ID inválido ('%s'); utilizando storeId (%s) como fallback.",
                    rawCatalog, fallbackStoreId);
            return fallbackStoreId;
        }
    }

    static String slugify(String text) {
        if (text == null || text.isBlank()) {
            return "unidade-balcao";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "unidade-balcao" : normalized;
    }
}
