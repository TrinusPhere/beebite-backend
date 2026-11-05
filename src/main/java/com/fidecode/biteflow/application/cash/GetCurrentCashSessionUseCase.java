package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.application.cash.dto.CashSessionDtoMapper;
import com.fidecode.biteflow.application.cash.dto.CashSessionResponse;
import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.exception.NoOpenSessionException;
import com.fidecode.biteflow.domain.cash.repository.CashSessionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetCurrentCashSessionUseCase {

    private final CashSessionRepository sessionRepository;

    @Inject
    public GetCurrentCashSessionUseCase(CashSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public CashSessionResponse execute(UUID storeId, UUID userId) {
        CashSession session = sessionRepository.findOpenSessionByUserId(storeId, userId)
                .orElseThrow(() -> new NoOpenSessionException("Nenhuma sessão de caixa aberta para este operador."));
        return CashSessionDtoMapper.toResponse(session);
    }
}
