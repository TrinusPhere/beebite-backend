package com.trinusphere.beebite.domain.cash.repository;

import com.trinusphere.beebite.domain.cash.entity.CashMovement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashMovementRepository {
    CashMovement save(CashMovement movement);
    Optional<CashMovement> findById(UUID id);
    List<CashMovement> findBySessionId(UUID sessionId);
}
