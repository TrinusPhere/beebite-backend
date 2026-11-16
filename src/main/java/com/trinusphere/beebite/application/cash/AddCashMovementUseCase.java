package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashMovementDtoMapper;
import com.trinusphere.beebite.application.cash.dto.CashMovementRequest;
import com.trinusphere.beebite.application.cash.dto.CashMovementResponse;
import com.trinusphere.beebite.domain.cash.entity.CashMovement;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.exception.CashSessionNotFoundException;
import com.trinusphere.beebite.domain.cash.exception.SessionAlreadyClosedException;
import com.trinusphere.beebite.domain.cash.repository.CashMovementRepository;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
import com.trinusphere.beebite.domain.cash.valueobject.CashSessionStatus;
import com.trinusphere.beebite.domain.cash.valueobject.MovementType;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class AddCashMovementUseCase {

    private final CashMovementRepository movementRepository;
    private final CashSessionRepository sessionRepository;

    @Inject
    public AddCashMovementUseCase(CashMovementRepository movementRepository,
                                  CashSessionRepository sessionRepository) {
        this.movementRepository = movementRepository;
        this.sessionRepository = sessionRepository;
    }

    public CashMovementResponse execute(UUID storeId, UUID sessionId, UUID userId, CashMovementRequest request) {
        CashSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CashSessionNotFoundException(sessionId.toString()));
        StoreGuard.enforce(session, storeId);

        if (session.getStatus() == CashSessionStatus.CLOSED) {
            throw new SessionAlreadyClosedException("Não é possível realizar movimentações em uma sessão fechada.");
        }

        MovementType type = MovementType.valueOf(request.type().toUpperCase());
        CashMovement movement = new CashMovement(storeId, sessionId, userId, type, request.amount(), request.reason());
        CashMovement saved = movementRepository.save(movement);
        return CashMovementDtoMapper.toResponse(saved);
    }
}
