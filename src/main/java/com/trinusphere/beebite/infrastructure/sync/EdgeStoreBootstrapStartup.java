package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.BootstrapEdgeStoreUseCase;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

/**
 * Executa o bootstrap automático da loja Edge logo após o {@code ProductionAdminSeedGuard} (prioridade 2500),
 * garantindo que o banco local nasça limpo, apenas com a loja real do cliente e usuário local como {@code ADMIN}.
 */
@ApplicationScoped
public class EdgeStoreBootstrapStartup {

    @Inject
    BootstrapEdgeStoreUseCase bootstrapEdgeStoreUseCase;

    void onStart(@Observes @Priority(Interceptor.Priority.APPLICATION + 600) StartupEvent event) {
        bootstrapEdgeStoreUseCase.execute();
    }
}

