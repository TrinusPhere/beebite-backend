package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.ReconciliationResult;
import com.trinusphere.beebite.domain.sync.exception.EdgeSyncBusyException;
import com.trinusphere.beebite.domain.sync.exception.InvalidSyncRequestException;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class TriggerEdgeReconciliationUseCase {

    private static final long MAX_WINDOW_DAYS = 31L;

    private final ReconcileEdgeDataUseCase reconcileEdgeDataUseCase;

    @Inject
    public TriggerEdgeReconciliationUseCase(ReconcileEdgeDataUseCase reconcileEdgeDataUseCase) {
        this.reconcileEdgeDataUseCase = reconcileEdgeDataUseCase;
    }

    public ReconciliationResult execute(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new InvalidSyncRequestException("Os parâmetros 'from' e 'to' são obrigatórios.");
        }
        if (from.isAfter(to)) {
            throw new InvalidSyncRequestException("A data inicial não pode ser posterior à data final.");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1L;
        if (days > MAX_WINDOW_DAYS) {
            throw new InvalidSyncRequestException("A janela de reconciliação manual não pode exceder 31 dias.");
        }

        return reconcileEdgeDataUseCase.execute(ReconciliationTrigger.MANUAL, from, to)
                .orElseThrow(() -> new EdgeSyncBusyException(
                        "Não é possível iniciar a conferência agora: há eventos pendentes na fila ou o fluxo está aguardando reconexão."));
    }
}

