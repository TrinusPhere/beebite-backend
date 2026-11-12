package com.fidecode.beebite.application.cash;

import com.fidecode.beebite.application.cash.dto.CashRegisterDtoMapper;
import com.fidecode.beebite.application.cash.dto.CashRegisterRequest;
import com.fidecode.beebite.application.cash.dto.CashRegisterResponse;
import com.fidecode.beebite.domain.cash.entity.CashRegister;
import com.fidecode.beebite.domain.cash.exception.CashRegisterNotFoundException;
import com.fidecode.beebite.domain.cash.repository.CashRegisterRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
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
