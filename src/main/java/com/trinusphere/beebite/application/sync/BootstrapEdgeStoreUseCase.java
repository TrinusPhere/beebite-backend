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
 * remove a loja/tenant seed padrão ({@code 00000000-0000-0000-0000-000000000001}) e garante que o
 * usuário inicial local seja {@code ADMIN} da loja (jamais {@code SUPER_ADMIN}).
 */
@ApplicationScoped
public class BootstrapEdgeStoreUseCase {

    private static final Logger LOG = Logger.getLogger(BootstrapEdgeStoreUseCase.class);
    static final UUID DEFAULT_SEED_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final EdgeStoreBootstrapPort bootstrapPort;
    private final String syncMode;
    private final Optional<String> edgeStoreIdRaw;
    private final String edgeStoreName;
    private final String edgeStoreSlug;
    private final String edgeStoreAddress;
    private final String edgeAdminLogin;

    @Inject
    public BootstrapEdgeStoreUseCase(
            EdgeStoreBootstrapPort bootstrapPort,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.edge.store-id") Optional<String> edgeStoreIdRaw,
            @ConfigProperty(name = "biteflow.edge.store-name", defaultValue = "") String edgeStoreName,
            @ConfigProperty(name = "biteflow.edge.store-slug", defaultValue = "") String edgeStoreSlug,
            @ConfigProperty(name = "biteflow.edge.store-address", defaultValue = "") String edgeStoreAddress,
            @ConfigProperty(name = "biteflow.edge.admin-login", defaultValue = "admin") String edgeAdminLogin) {
        this.bootstrapPort = bootstrapPort;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase(Locale.ROOT) : "CLOUD";
        this.edgeStoreIdRaw = edgeStoreIdRaw != null ? edgeStoreIdRaw : Optional.empty();
        this.edgeStoreName = edgeStoreName;
        this.edgeStoreSlug = edgeStoreSlug;
        this.edgeStoreAddress = edgeStoreAddress;
        this.edgeAdminLogin = edgeAdminLogin;
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

        String resolvedName = (edgeStoreName != null && !edgeStoreName.isBlank())
                ? edgeStoreName.trim()
                : "Unidade Balcão";
        String resolvedSlug = (edgeStoreSlug != null && !edgeStoreSlug.isBlank())
                ? edgeStoreSlug.trim().toLowerCase(Locale.ROOT)
                : slugify(resolvedName);
        String resolvedAddress = (edgeStoreAddress != null && !edgeStoreAddress.isBlank())
                ? edgeStoreAddress.trim()
                : "Unidade Presencial / Balcão";
        String resolvedAdminLogin = (edgeAdminLogin != null && !edgeAdminLogin.isBlank())
                ? edgeAdminLogin.trim().toLowerCase(Locale.ROOT)
                : "admin";

        bootstrapPort.bootstrapClientStoreAndCleanSeed(
                edgeStoreId,
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

        LOG.infof("[EDGE-BOOTSTRAP] Unidade Edge '%s' (%s) inicializada com sucesso como loja única local (admin=%s).",
                resolvedName, edgeStoreId, resolvedAdminLogin);
        return true;
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
