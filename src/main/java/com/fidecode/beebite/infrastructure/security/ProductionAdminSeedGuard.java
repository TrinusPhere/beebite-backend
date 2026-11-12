package com.fidecode.beebite.infrastructure.security;

import com.fidecode.beebite.application.auth.port.PasswordEncoder;
import com.fidecode.beebite.domain.auth.entity.User;
import com.fidecode.beebite.domain.auth.repository.UserRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Optional;

/**
 * Guardião de segurança para produção (DevSecOps):
 * Garante que o usuário seed 'admin' criado pela V1__foundation.sql nunca permaneça
 * ativo com a senha pública 'admin123' em ambiente de produção.
 */
@ApplicationScoped
public class ProductionAdminSeedGuard {

    private static final Logger LOG = Logger.getLogger(ProductionAdminSeedGuard.class);
    static final String DEFAULT_SEED_HASH = "$2a$10$FHNmuZPizma6NEjYV.1.HOWogQSpS0ZbuEJHGpsCodoFhzbvpuEv6";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enforceProdGuards;
    private final Optional<String> initialAdminPassword;

    @Inject
    public ProductionAdminSeedGuard(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @ConfigProperty(name = "biteflow.security.enforce-prod-guards", defaultValue = "true") boolean enforceProdGuards,
            @ConfigProperty(name = "biteflow.security.initial-admin-password") Optional<String> initialAdminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enforceProdGuards = enforceProdGuards;
        this.initialAdminPassword = initialAdminPassword;
    }

    void onStart(@Observes StartupEvent event) {
        enforceSeedSecurity();
    }

    @Transactional
    public void enforceSeedSecurity() {
        if (!enforceProdGuards) {
            return;
        }

        userRepository.findByLogin("admin").ifPresent(admin -> {
            if (!DEFAULT_SEED_HASH.equals(admin.getPasswordHash())) {
                return;
            }

            if (initialAdminPassword.isPresent()
                    && !initialAdminPassword.get().isBlank()
                    && !"admin123".equals(initialAdminPassword.get())) {
                admin.changePasswordHash(passwordEncoder.hash(initialAdminPassword.get()));
                admin.activate();
                userRepository.save(admin);
                LOG.info("[SECURITY-GUARD] Senha do usuário seed 'admin' rotacionada com sucesso para produção.");
            } else {
                admin.deactivate();
                userRepository.save(admin);
                LOG.warn("[SECURITY-GUARD] Usuário seed 'admin' estava com a senha padrão pública (admin123) em produção e foi DESATIVADO. Defina BITEFLOW_INITIAL_ADMIN_PASSWORD para ativá-lo com uma senha forte.");
            }
        });
    }
}

