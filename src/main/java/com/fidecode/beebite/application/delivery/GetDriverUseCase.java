package com.fidecode.beebite.application.delivery;

import com.fidecode.beebite.application.delivery.dto.DriverResponse;
import com.fidecode.beebite.domain.delivery.entity.DeliveryDriver;
import com.fidecode.beebite.domain.delivery.exception.DriverNotFoundException;
import com.fidecode.beebite.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
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
