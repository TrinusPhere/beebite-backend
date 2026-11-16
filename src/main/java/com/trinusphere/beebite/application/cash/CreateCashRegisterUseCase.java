package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashRegisterDtoMapper;
import com.trinusphere.beebite.application.cash.dto.CashRegisterRequest;
import com.trinusphere.beebite.application.cash.dto.CashRegisterResponse;
import com.trinusphere.beebite.domain.cash.entity.CashRegister;
import com.trinusphere.beebite.domain.cash.repository.CashRegisterRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CreateCashRegisterUseCase {

    private final CashRegisterRepository repository;

    @Inject
    public CreateCashRegisterUseCase(CashRegisterRepository repository) {
        this.repository = repository;
    }

    public CashRegisterResponse execute(UUID storeId, CashRegisterRequest request) {
        CashRegister register = new CashRegister(storeId, request.name());
        CashRegister saved = repository.save(register);
        return CashRegisterDtoMapper.toResponse(saved);
    }
}
