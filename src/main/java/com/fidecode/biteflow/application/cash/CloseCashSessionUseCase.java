package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.application.cash.dto.CashSessionDtoMapper;
import com.fidecode.biteflow.application.cash.dto.CashSessionResponse;
import com.fidecode.biteflow.application.cash.dto.CloseCashSessionRequest;
import com.fidecode.biteflow.domain.cash.entity.CashMovement;
import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.exception.CashSessionNotFoundException;
import com.fidecode.biteflow.domain.cash.repository.CashMovementRepository;
import com.fidecode.biteflow.domain.cash.repository.CashSessionRepository;
import com.fidecode.biteflow.domain.cash.valueobject.MovementType;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import com.fidecode.biteflow.domain.payment.entity.Payment;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import com.fidecode.biteflow.domain.payment.valueobject.PaymentStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CloseCashSessionUseCase {

    private final CashSessionRepository sessionRepository;
    private final CashMovementRepository movementRepository;
    private final PaymentRepository paymentRepository;

    @Inject
    public CloseCashSessionUseCase(CashSessionRepository sessionRepository,
                                  CashMovementRepository movementRepository,
                                  PaymentRepository paymentRepository) {
        this.sessionRepository = sessionRepository;
        this.movementRepository = movementRepository;
        this.paymentRepository = paymentRepository;
    }

    public CashSessionResponse execute(UUID storeId, UUID sessionId, CloseCashSessionRequest request) {
        CashSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CashSessionNotFoundException(sessionId.toString()));
        StoreGuard.enforce(session, storeId);

        List<CashMovement> movements = movementRepository.findBySessionId(sessionId);
        List<Payment> payments = paymentRepository.findByCashSessionId(sessionId);

        BigDecimal cashInRegister = session.getInitialAmount();

        // Soma entradas e saídas de movimentações
        for (CashMovement m : movements) {
            if (m.getType() == MovementType.SUPRIMENTO) {
                cashInRegister = cashInRegister.add(m.getAmount());
            } else if (m.getType() == MovementType.SANGRIA) {
                cashInRegister = cashInRegister.subtract(m.getAmount());
            } else if (m.getType() == MovementType.AJUSTE_COMPENSATORIO) {
                cashInRegister = cashInRegister.add(m.getAmount());
            }
        }

        // Soma pagamentos em DINHEIRO (tanto recebidos no caixa quanto conferidos de entregadores)
        for (Payment p : payments) {
            if (p.getStatus() == PaymentStatus.CONFIRMED && p.getMethod() == PaymentMethod.DINHEIRO) {
                cashInRegister = cashInRegister.add(p.getAmount());
            }
        }

        session.close(cashInRegister, request.countedAmount(), request.closingNotes());
        CashSession saved = sessionRepository.save(session);
        return CashSessionDtoMapper.toResponse(saved);
    }
}
