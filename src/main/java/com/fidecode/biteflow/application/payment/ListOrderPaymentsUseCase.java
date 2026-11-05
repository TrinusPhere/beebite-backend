package com.fidecode.biteflow.application.payment;

import com.fidecode.biteflow.application.payment.dto.PaymentDtoMapper;
import com.fidecode.biteflow.application.payment.dto.PaymentResponse;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListOrderPaymentsUseCase {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Inject
    public ListOrderPaymentsUseCase(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public List<PaymentResponse> execute(UUID storeId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        StoreGuard.enforce(order, storeId);

        return paymentRepository.findByOrderId(orderId).stream()
                .map(PaymentDtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
