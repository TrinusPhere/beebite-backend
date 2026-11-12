package com.fidecode.beebite.application.cash;

import com.fidecode.beebite.application.cash.dto.CashRegisterDtoMapper;
import com.fidecode.beebite.application.cash.dto.CashRegisterRequest;
import com.fidecode.beebite.application.cash.dto.CashRegisterResponse;
import com.fidecode.beebite.domain.cash.entity.CashRegister;
import com.fidecode.beebite.domain.cash.repository.CashRegisterRepository;
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
