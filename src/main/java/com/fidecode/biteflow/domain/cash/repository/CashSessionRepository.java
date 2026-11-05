package com.fidecode.biteflow.domain.cash.repository;

import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashSessionRepository {
    CashSession save(CashSession session);
    Optional<CashSession> findById(UUID id);
    Optional<CashSession> findOpenSessionByUserId(UUID storeId, UUID userId);
    Optional<CashSession> findOpenSessionByRegisterId(UUID storeId, UUID registerId);
    List<CashSession> findByStoreIdAndPeriod(UUID storeId, LocalDateTime start, LocalDateTime end);
    List<CashSession> findByStoreIdAndStatus(UUID storeId, CashSessionStatus status);
}
