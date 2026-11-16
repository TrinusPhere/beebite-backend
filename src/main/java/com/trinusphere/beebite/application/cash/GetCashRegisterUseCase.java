package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashRegisterDtoMapper;
import com.trinusphere.beebite.application.cash.dto.CashRegisterResponse;
import com.trinusphere.beebite.domain.cash.entity.CashRegister;
import com.trinusphere.beebite.domain.cash.exception.CashRegisterNotFoundException;
import com.trinusphere.beebite.domain.cash.repository.CashRegisterRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetCashRegisterUseCase {

    private final CashRegisterRepository repository;

    @Inject
    public GetCashRegisterUseCase(CashRegisterRepository repository) {
        this.repository = repository;
    }

    public CashRegisterResponse execute(UUID storeId, UUID id) {
        CashRegister register = repository.findById(id)
                .orElseThrow(() -> new CashRegisterNotFoundException(id.toString()));
        StoreGuard.enforce(register, storeId);
        return CashRegisterDtoMapper.toResponse(register);
    }
}
