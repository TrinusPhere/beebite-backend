package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.application.cash.dto.CashMovementDtoMapper;
import com.fidecode.biteflow.application.cash.dto.CashMovementRequest;
import com.fidecode.biteflow.application.cash.dto.CashMovementResponse;
import com.fidecode.biteflow.domain.cash.entity.CashMovement;
import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.exception.CashSessionNotFoundException;
import com.fidecode.biteflow.domain.cash.exception.SessionAlreadyClosedException;
import com.fidecode.biteflow.domain.cash.repository.CashMovementRepository;
import com.fidecode.biteflow.domain.cash.repository.CashSessionRepository;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;
import com.fidecode.biteflow.domain.cash.valueobject.MovementType;
import com.fidecode.biteflow.domain.shared.StoreGuard;
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
