package com.fidecode.biteflow.domain.cash.repository;

import com.fidecode.biteflow.domain.cash.entity.CashRegister;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashRegisterRepository {
    CashRegister save(CashRegister register);
    Optional<CashRegister> findById(UUID id);
    List<CashRegister> findAllByStoreId(UUID storeId);
    List<CashRegister> findActiveByStoreId(UUID storeId);
    void delete(CashRegister register);
}
