package com.fidecode.beebite.application.delivery;

import com.fidecode.beebite.application.delivery.dto.DriverRequest;
import com.fidecode.beebite.application.delivery.dto.DriverResponse;
import com.fidecode.beebite.domain.delivery.entity.DeliveryDriver;
import com.fidecode.beebite.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.beebite.domain.delivery.valueobject.DriverStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CreateDriverUseCase {

    private final DeliveryDriverRepository repository;

    @Inject
    public CreateDriverUseCase(DeliveryDriverRepository repository) {
        this.repository = repository;
    }

    public DriverResponse execute(UUID storeId, DriverRequest request) {
        DeliveryDriver driver = new DeliveryDriver(storeId, request.name(), request.phone());

        driver.configureWork(request.status() != null ? DriverStatus.valueOf(request.status()) : null,
                request.isFixed(), request.isDaily(), request.allowance(), request.hasFixedDeliveryValue(),
                request.fixedDeliveryValue());

        return DriverDtoMapper.toResponse(repository.save(driver));
    }
}
