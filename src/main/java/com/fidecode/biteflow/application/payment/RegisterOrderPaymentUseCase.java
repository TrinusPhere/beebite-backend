package com.fidecode.biteflow.application.payment;

import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.repository.CashSessionRepository;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;
import com.fidecode.biteflow.domain.financial.event.OrderFinalizedEvent;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import com.fidecode.biteflow.domain.payment.entity.Payment;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import com.fidecode.biteflow.domain.payment.valueobject.PaymentCollector;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RegisterOrderPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final CashSessionRepository cashSessionRepository;
    private final OrderRepository orderRepository;

    @Inject
    public RegisterOrderPaymentUseCase(PaymentRepository paymentRepository,
                                       CashSessionRepository cashSessionRepository,
                                       OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.cashSessionRepository = cashSessionRepository;
        this.orderRepository = orderRepository;
    }

    public void execute(OrderFinalizedEvent event) {
        if (event == null || event.getOrderId() == null) {
            return;
        }

        List<CashSession> openSessions = cashSessionRepository.findByStoreIdAndStatus(event.getStoreId(), CashSessionStatus.OPEN);
        UUID openSessionId = openSessions.isEmpty() ? null : openSessions.get(0).getId();

        List<Payment> existingPayments = paymentRepository.findByOrderId(event.getOrderId());

        if (existingPayments.isEmpty()) {
            String displayId = orderRepository.findById(event.getOrderId())
                    .map(Order::getDisplayId)
                    .orElse(null);
            String notes = displayId != null && !displayId.isBlank()
                    ? "Recebimento do pedido #" + displayId
                    : "Recebimento de pedido";

            PaymentMethod method = event.getPaymentMethod() != null ? event.getPaymentMethod() : PaymentMethod.DINHEIRO;
            PaymentCollector collector = event.getDriverId() != null ? PaymentCollector.ENTREGADOR : PaymentCollector.CAIXA;

            Payment payment = new Payment(
                    event.getStoreId(),
                    event.getOrderId(),
                    openSessionId,
                    event.getDriverId(),
                    method,
                    event.getTotalValue(),
                    null,
                    collector,
                    notes
            );
            paymentRepository.save(payment);
        } else if (openSessionId != null) {
            for (Payment p : existingPayments) {
                if (p.getCashSessionId() == null) {
                    p.linkSession(openSessionId);
                    paymentRepository.save(p);
                }
            }
        }
    }
}

