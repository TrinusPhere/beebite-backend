package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashSessionDtoMapper;
import com.trinusphere.beebite.application.cash.dto.CashSessionResponse;
import com.trinusphere.beebite.application.cash.dto.CloseCashSessionRequest;
import com.trinusphere.beebite.domain.cash.entity.CashMovement;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.exception.CashSessionNotFoundException;
import com.trinusphere.beebite.domain.cash.repository.CashMovementRepository;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
import com.trinusphere.beebite.domain.cash.valueobject.MovementType;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import com.trinusphere.beebite.domain.payment.entity.Payment;
import com.trinusphere.beebite.domain.payment.repository.PaymentRepository;
import com.trinusphere.beebite.domain.payment.valueobject.PaymentStatus;
import com.trinusphere.beebite.domain.shared.StoreGuard;
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
    private final OrderRepository orderRepository;

    public CloseCashSessionUseCase(CashSessionRepository sessionRepository,
                                  CashMovementRepository movementRepository,
                                  PaymentRepository paymentRepository) {
        this(sessionRepository, movementRepository, paymentRepository, null);
    }

    @Inject
    public CloseCashSessionUseCase(CashSessionRepository sessionRepository,
                                  CashMovementRepository movementRepository,
                                  PaymentRepository paymentRepository,
                                  OrderRepository orderRepository) {
        this.sessionRepository = sessionRepository;
        this.movementRepository = movementRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
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

        // Reinicia a numeração de pedidos do PDV para a próxima sessão de caixa
        if (orderRepository != null) {
            orderRepository.resetDisplayIdSequence();
        }

        return CashSessionDtoMapper.toResponse(saved);
    }
}
