package com.fidecode.biteflow.application.order;
import com.fidecode.biteflow.application.order.dto.PublicOrderReviewInfoResponse;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
@ApplicationScoped public class GetPublicOrderReviewInfoUseCase {
 private final OrderRepository repository;
 @Inject public GetPublicOrderReviewInfoUseCase(OrderRepository repository){this.repository=repository;}
 public PublicOrderReviewInfoResponse execute(UUID orderId){var order=repository.findById(orderId).orElseThrow(()->new OrderNotFoundException(orderId.toString()));return new PublicOrderReviewInfoResponse(order.getId().toString(),order.getDisplayId(),order.getDriverName());}
}
