package com.fidecode.biteflow.application.delivery;

import com.fidecode.biteflow.application.delivery.dto.DriverResponse;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.exception.DriverNotFoundException;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetDriverUseCase {

    private final DeliveryDriverRepository repository;

    @Inject
    public GetDriverUseCase(DeliveryDriverRepository repository) {
        this.repository = repository;
    }

    public DriverResponse execute(UUID storeId, UUID id) {
        DeliveryDriver driver = repository.findById(id).orElseThrow(() -> new DriverNotFoundException(id.toString()));
        StoreGuard.enforce(driver, storeId);
        return DriverDtoMapper.toResponse(driver);
    }
}
