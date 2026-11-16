package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashSessionDtoMapper;
import com.trinusphere.beebite.application.cash.dto.CashSessionResponse;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.exception.NoOpenSessionException;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
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
