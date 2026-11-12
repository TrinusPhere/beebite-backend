package com.fidecode.beebite.application.cash;

import com.fidecode.beebite.domain.cash.entity.CashRegister;
import com.fidecode.beebite.domain.cash.exception.CashRegisterNotFoundException;
import com.fidecode.beebite.domain.cash.repository.CashRegisterRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
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
