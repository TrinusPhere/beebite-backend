package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashSessionDtoMapper;
import com.trinusphere.beebite.application.cash.dto.CashSessionResponse;
import com.trinusphere.beebite.application.cash.dto.OpenCashSessionRequest;
import com.trinusphere.beebite.domain.cash.entity.CashRegister;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.exception.ActiveSessionAlreadyExistsException;
import com.trinusphere.beebite.domain.cash.exception.CashRegisterNotFoundException;
import com.trinusphere.beebite.domain.cash.repository.CashRegisterRepository;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class OpenCashSessionUseCase {

    private final CashSessionRepository sessionRepository;
    private final CashRegisterRepository registerRepository;

    @Inject
    public OpenCashSessionUseCase(CashSessionRepository sessionRepository,
                                  CashRegisterRepository registerRepository) {
        this.sessionRepository = sessionRepository;
        this.registerRepository = registerRepository;
    }

    public CashSessionResponse execute(UUID storeId, UUID userId, OpenCashSessionRequest request) {
        // Valida se o usuário já possui um caixa aberto (Regra de Negócio)
        sessionRepository.findOpenSessionByUserId(storeId, userId).ifPresent(s -> {
            throw new ActiveSessionAlreadyExistsException("O operador já possui uma sessão de caixa aberta.");
        });

        UUID registerId = UUID.fromString(request.cashRegisterId());
        CashRegister register = registerRepository.findById(registerId)
                .orElseThrow(() -> new CashRegisterNotFoundException(registerId.toString()));
        StoreGuard.enforce(register, storeId);

        // Valida se o caixa físico já está em uso
        sessionRepository.findOpenSessionByRegisterId(storeId, registerId).ifPresent(s -> {
            throw new ActiveSessionAlreadyExistsException("Este terminal de caixa já possui uma sessão aberta.");
        });

        CashSession session = new CashSession(storeId, registerId, userId, request.initialAmount());
        CashSession saved = sessionRepository.save(session);
        return CashSessionDtoMapper.toResponse(saved);
    }
}
