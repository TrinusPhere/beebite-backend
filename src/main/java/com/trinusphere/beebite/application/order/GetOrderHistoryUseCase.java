package com.trinusphere.beebite.application.order;

import com.trinusphere.beebite.application.order.dto.OrderResponse;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GetOrderHistoryUseCase {

    private final OrderRepository orderRepository;

    @Inject
    public GetOrderHistoryUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderResponse> execute(UUID storeId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(23, 59, 59, 999999999);

        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, start, end);
        return orders.stream()
                .map(OrderDtoMapper::toResponse)
                .toList();
    }
}
