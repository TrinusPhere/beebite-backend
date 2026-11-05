package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.application.cash.dto.CashRegisterDtoMapper;
import com.fidecode.biteflow.application.cash.dto.CashRegisterRequest;
import com.fidecode.biteflow.application.cash.dto.CashRegisterResponse;
import com.fidecode.biteflow.domain.cash.entity.CashRegister;
import com.fidecode.biteflow.domain.cash.exception.CashRegisterNotFoundException;
import com.fidecode.biteflow.domain.cash.repository.CashRegisterRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateCashRegisterUseCase {

    private final CashRegisterRepository repository;

    @Inject
    public UpdateCashRegisterUseCase(CashRegisterRepository repository) {
        this.repository = repository;
    }

    public CashRegisterResponse execute(UUID storeId, UUID id, CashRegisterRequest request) {
        CashRegister register = repository.findById(id)
                .orElseThrow(() -> new CashRegisterNotFoundException(id.toString()));
        StoreGuard.enforce(register, storeId);

        register.rename(request.name());
        CashRegister saved = repository.save(register);
        return CashRegisterDtoMapper.toResponse(saved);
    }
}
