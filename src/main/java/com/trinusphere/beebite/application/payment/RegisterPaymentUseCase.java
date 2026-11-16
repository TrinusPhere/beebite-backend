package com.trinusphere.beebite.application.payment;

import com.trinusphere.beebite.application.payment.dto.PaymentDtoMapper;
import com.trinusphere.beebite.application.payment.dto.PaymentRequest;
import com.trinusphere.beebite.application.payment.dto.PaymentResponse;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.exception.OrderNotFoundException;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import com.trinusphere.beebite.domain.payment.entity.Payment;
import com.trinusphere.beebite.domain.payment.repository.PaymentRepository;
import com.trinusphere.beebite.domain.payment.valueobject.PaymentCollector;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class RegisterPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Inject
    public RegisterPaymentUseCase(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public PaymentResponse execute(UUID storeId, PaymentRequest request) {
        UUID orderId = request.orderId() != null && !request.orderId().isBlank()
                ? UUID.fromString(request.orderId()) : null;

        if (orderId != null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
            StoreGuard.enforce(order, storeId);
        }

        UUID sessionId = request.cashSessionId() != null && !request.cashSessionId().isBlank()
                ? UUID.fromString(request.cashSessionId()) : null;

        UUID driverId = request.driverId() != null && !request.driverId().isBlank()
                ? UUID.fromString(request.driverId()) : null;

        PaymentMethod method = parsePaymentMethod(request.method());
        PaymentCollector collector = request.collectedBy() != null && !request.collectedBy().isBlank()
                ? PaymentCollector.valueOf(request.collectedBy().toUpperCase())
                : (driverId != null ? PaymentCollector.ENTREGADOR : PaymentCollector.CAIXA);

        Payment payment = new Payment(
                storeId, orderId, sessionId, driverId, method,
                request.amount(), request.changeFor(), collector, request.notes()
        );

        Payment saved = paymentRepository.save(payment);
        return PaymentDtoMapper.toResponse(saved);
    }

    private PaymentMethod parsePaymentMethod(String method) {
        if (method == null) return PaymentMethod.DINHEIRO;
        return switch (method.toUpperCase()) {
            case "CREDIT_CARD", "CREDITO" -> PaymentMethod.CREDITO;
            case "DEBIT_CARD", "DEBITO" -> PaymentMethod.DEBITO;
            case "CASH", "DINHEIRO" -> PaymentMethod.DINHEIRO;
            case "PIX" -> PaymentMethod.PIX;
            default -> PaymentMethod.valueOf(method.toUpperCase());
        };
    }
}
