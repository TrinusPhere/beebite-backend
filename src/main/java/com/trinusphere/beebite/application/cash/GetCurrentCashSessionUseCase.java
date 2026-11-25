package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashSessionDtoMapper;
import com.trinusphere.beebite.application.cash.dto.CashSessionResponse;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.exception.NoOpenSessionException;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
import com.trinusphere.beebite.domain.cash.valueobject.CashSessionStatus;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GetCurrentCashSessionUseCase {

    private final CashSessionRepository sessionRepository;
    private final StoreRepository storeRepository;
    private final String syncMode;

    public GetCurrentCashSessionUseCase(CashSessionRepository sessionRepository) {
        this(sessionRepository, null, "CLOUD");
    }

    @Inject
    public GetCurrentCashSessionUseCase(
            CashSessionRepository sessionRepository,
            StoreRepository storeRepository,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode) {
        this.sessionRepository = sessionRepository;
        this.storeRepository = storeRepository;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
    }

    public CashSessionResponse execute(UUID storeId, UUID userId) {
        CashSession session = sessionRepository.findOpenSessionByUserId(storeId, userId)
                .or(() -> findCloudMonitoredEdgeSession(storeId))
                .orElseThrow(() -> new NoOpenSessionException("Nenhuma sessão de caixa aberta para este operador."));
        return CashSessionDtoMapper.toResponse(session);
    }

    private Optional<CashSession> findCloudMonitoredEdgeSession(UUID storeId) {
        if ("EDGE".equals(syncMode) || storeRepository == null || storeId == null) {
            return Optional.empty();
        }
        return storeRepository.findById(storeId)
                .filter(store -> "EDGE".equalsIgnoreCase(store.getSyncMode()))
                .flatMap(store -> sessionRepository.findByStoreIdAndStatus(storeId, CashSessionStatus.OPEN)
                        .stream()
                        .findFirst());
    }
}
