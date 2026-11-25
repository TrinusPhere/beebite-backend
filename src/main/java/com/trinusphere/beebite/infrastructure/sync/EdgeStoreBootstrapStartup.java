package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.BackfillEdgeOutboxUseCase;
import com.trinusphere.beebite.application.sync.BootstrapEdgeStoreUseCase;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

/**
 * Executa o bootstrap automático da loja Edge logo após o {@code ProductionAdminSeedGuard} (prioridade 2500),
 * garantindo que o banco local nasça limpo, apenas com a loja real do cliente e usuário local como {@code ADMIN},
 * e enfileira na Outbox quaisquer operações locais ainda não sincronizadas.
 */
@ApplicationScoped
public class EdgeStoreBootstrapStartup {

    @Inject
    BootstrapEdgeStoreUseCase bootstrapEdgeStoreUseCase;

    @Inject
    BackfillEdgeOutboxUseCase backfillEdgeOutboxUseCase;

    void onStart(@Observes @Priority(Interceptor.Priority.APPLICATION + 600) StartupEvent event) {
        bootstrapEdgeStoreUseCase.execute();
        backfillEdgeOutboxUseCase.execute();
    }
}
