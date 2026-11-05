package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.domain.cash.entity.CashRegister;
import com.fidecode.biteflow.domain.cash.exception.CashRegisterNotFoundException;
import com.fidecode.biteflow.domain.cash.repository.CashRegisterRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteCashRegisterUseCase {

    private final CashRegisterRepository repository;

    @Inject
    public DeleteCashRegisterUseCase(CashRegisterRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID storeId, UUID id) {
        CashRegister register = repository.findById(id)
                .orElseThrow(() -> new CashRegisterNotFoundException(id.toString()));
        StoreGuard.enforce(register, storeId);

        register.markDeleted();
        repository.save(register);
    }
}
