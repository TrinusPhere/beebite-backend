package com.trinusphere.beebite.application.order;

import com.trinusphere.beebite.application.order.dto.PublicOrderReviewInfoResponse;
import com.trinusphere.beebite.domain.order.exception.OrderNotFoundException;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class GetPublicOrderReviewInfoUseCase {
    private final OrderRepository repository;

    @Inject
    public GetPublicOrderReviewInfoUseCase(OrderRepository repository) {
        this.repository = repository;
    }

    public PublicOrderReviewInfoResponse execute(UUID orderId) {
        var order = repository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        return new PublicOrderReviewInfoResponse(order.getId().toString(), order.getDisplayId(), order.getDriverName(),
                order.getCustomerName(), order.getCustomerPhone());
    }
}
