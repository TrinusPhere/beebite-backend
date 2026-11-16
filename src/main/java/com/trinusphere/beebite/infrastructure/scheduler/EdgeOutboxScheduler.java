package com.trinusphere.beebite.infrastructure.scheduler;

import com.trinusphere.beebite.application.sync.FlushEdgeOutboxUseCase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EdgeOutboxScheduler {

    private static final Logger LOG = Logger.getLogger(EdgeOutboxScheduler.class);

    @Inject
    FlushEdgeOutboxUseCase flushEdgeOutboxUseCase;

    @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD")
    String syncMode;

    @Scheduled(every = "${biteflow.sync.poll-interval:15s}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void pollAndFlushOutbox() {
        if (!"EDGE".equalsIgnoreCase(syncMode != null ? syncMode.trim() : "CLOUD")) {
            return;
        }
        int synced = flushEdgeOutboxUseCase.execute();
        if (synced > 0) {
            LOG.infof("Sincronização Edge -> Cloud concluída: %d evento(s) enviados.", synced);
        }
    }
}
