package com.fidecode.beebite.application.order;

import com.fidecode.beebite.application.order.dto.OrderResponse;
import com.fidecode.beebite.application.shared.PageResponse;
import com.fidecode.beebite.domain.order.entity.Order;
import com.fidecode.beebite.domain.order.repository.OrderRepository;
import com.fidecode.beebite.domain.order.valueobject.OrderStatus;
import com.fidecode.beebite.domain.shared.PageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;

    @Inject
    public ListOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public PageResponse<OrderResponse> list(UUID storeId, String statusFilter, int page, int size) {
        PageResult<Order> orderPage;

        if (statusFilter != null && !statusFilter.isBlank()) {
            OrderStatus status = OrderStatus.valueOf(statusFilter);
            orderPage = orderRepository.findByStoreIdAndStatus(storeId, status, page, size);
        } else {
            orderPage = orderRepository.findAllByStoreId(storeId, page, size);
        }

        List<OrderResponse> content = orderPage.getContent().stream().map(OrderDtoMapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(content, orderPage.getPage(), orderPage.getSize(), orderPage.getTotalElements(),
                orderPage.getTotalPages());
    }
}
