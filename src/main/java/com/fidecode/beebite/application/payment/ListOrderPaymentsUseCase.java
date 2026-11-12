package com.fidecode.beebite.application.payment;

import com.fidecode.beebite.application.payment.dto.PaymentDtoMapper;
import com.fidecode.beebite.application.payment.dto.PaymentResponse;
import com.fidecode.beebite.domain.order.entity.Order;
import com.fidecode.beebite.domain.order.exception.OrderNotFoundException;
import com.fidecode.beebite.domain.order.repository.OrderRepository;
import com.fidecode.beebite.domain.payment.repository.PaymentRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
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
