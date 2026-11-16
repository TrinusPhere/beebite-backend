package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashMovementDtoMapper;
import com.trinusphere.beebite.application.cash.dto.CashMovementResponse;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.exception.CashSessionNotFoundException;
import com.trinusphere.beebite.domain.cash.repository.CashMovementRepository;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListCashMovementsUseCase {

    private final CashMovementRepository movementRepository;
    private final CashSessionRepository sessionRepository;

    @Inject
    public ListCashMovementsUseCase(CashMovementRepository movementRepository,
                                    CashSessionRepository sessionRepository) {
        this.movementRepository = movementRepository;
        this.sessionRepository = sessionRepository;
    }

    public List<CashMovementResponse> execute(UUID storeId, UUID sessionId) {
        CashSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CashSessionNotFoundException(sessionId.toString()));
        StoreGuard.enforce(session, storeId);

        return movementRepository.findBySessionId(sessionId).stream()
                .map(CashMovementDtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
