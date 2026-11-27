package com.trinusphere.beebite.infrastructure.scheduler;

import com.trinusphere.beebite.application.sync.FlushEdgeOutboxUseCase;
import com.trinusphere.beebite.application.sync.ProbeEdgeSyncCursorUseCase;
import com.trinusphere.beebite.application.sync.PullEdgeCatalogDeltaUseCase;
import com.trinusphere.beebite.application.sync.PurgeEdgeSyncHistoryUseCase;
import com.trinusphere.beebite.application.sync.ReconcileEdgeDataUseCase;
import com.trinusphere.beebite.application.sync.RunRequestedFullResyncUseCase;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.ZoneOffset;

@ApplicationScoped
public class EdgeOutboxScheduler {

    private static final Logger LOG = Logger.getLogger(EdgeOutboxScheduler.class);

    @Inject
    FlushEdgeOutboxUseCase flushEdgeOutboxUseCase;

    @Inject
    PullEdgeCatalogDeltaUseCase pullEdgeCatalogDeltaUseCase;

    @Inject
    ProbeEdgeSyncCursorUseCase probeEdgeSyncCursorUseCase;

    @Inject
    ReconcileEdgeDataUseCase reconcileEdgeDataUseCase;

    @Inject
    RunRequestedFullResyncUseCase runRequestedFullResyncUseCase;

    @Inject
    PurgeEdgeSyncHistoryUseCase purgeEdgeSyncHistoryUseCase;

    @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD")
    String syncMode;

    @ConfigProperty(name = "biteflow.sync.reconcile-window-days", defaultValue = "7")
    int reconcileWindowDays;

    @Scheduled(every = "${biteflow.sync.poll-interval:15s}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void pollAndFlushOutbox() {
        if (!isEdge()) {
            return;
        }
        int synced = flushEdgeOutboxUseCase.execute();
        if (synced > 0) {
            LOG.infof("Sincronização Edge -> Cloud concluída: %d evento(s) enviados.", synced);
        }

        int pulled = pullEdgeCatalogDeltaUseCase.execute();
        if (pulled > 0) {
            LOG.infof("Sincronização Cloud -> Edge (Cardápio) concluída: %d item(ns) aplicados localmente.", pulled);
        }
    }

    @Scheduled(every = "${biteflow.sync.cursor-probe-interval:5m}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void probeCursor() {
        if (!isEdge()) {
            return;
        }
        boolean reopened = probeEdgeSyncCursorUseCase.execute();
        if (reopened) {
            LOG.warn("Probe de cursor detectou eventos ausentes na nuvem; reenvio agendado.");
        }
    }

    @Scheduled(
            every = "${biteflow.sync.reconcile-interval:6h}",
            delayed = "2m",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void reconcile() {
        if (!isEdge()) {
            return;
        }
        int safeDays = Math.max(1, Math.min(31, reconcileWindowDays));
        LocalDate to = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = to.minusDays(safeDays - 1L);
        reconcileEdgeDataUseCase.execute(ReconciliationTrigger.SCHEDULED, from, to)
                .ifPresent(res -> LOG.infof(
                        "Reconciliação periódica concluída: status=%s, buckets=%d/%d, reenfileirados=%d",
                        res.status(), res.bucketsMismatched(), res.bucketsCompared(), res.requeuedCount()));
    }

    @Scheduled(every = "1m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void fullResyncIfRequested() {
        if (!isEdge()) {
            return;
        }
        if (runRequestedFullResyncUseCase.execute()) {
            LOG.infof("Reconciliação completa solicitada pela nuvem (FULL_RESYNC) concluída.");
        }
    }

    @Scheduled(cron = "${biteflow.sync.purge-cron:0 30 3 * * ?}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void purge() {
        int removed = purgeEdgeSyncHistoryUseCase.execute();
        if (removed > 0) {
            LOG.infof("Expurgo de histórico de sincronização concluído: %d registro(s) removidos.", removed);
        }
    }

    private boolean isEdge() {
        return "EDGE".equalsIgnoreCase(syncMode != null ? syncMode.trim() : "CLOUD");
    }
}
