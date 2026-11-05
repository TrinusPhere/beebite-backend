package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.application.cash.dto.CashRegisterDtoMapper;
import com.fidecode.biteflow.application.cash.dto.CashRegisterResponse;
import com.fidecode.biteflow.domain.cash.repository.CashRegisterRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListCashRegistersUseCase {

    private final CashRegisterRepository repository;

    @Inject
    public ListCashRegistersUseCase(CashRegisterRepository repository) {
        this.repository = repository;
    }

    public List<CashRegisterResponse> execute(UUID storeId) {
        return repository.findActiveByStoreId(storeId).stream()
                .map(CashRegisterDtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
