package com.trinusphere.beebite.application.delivery;

import com.trinusphere.beebite.application.delivery.dto.DriverRequest;
import com.trinusphere.beebite.application.delivery.dto.DriverResponse;
import com.trinusphere.beebite.domain.delivery.entity.DeliveryDriver;
import com.trinusphere.beebite.domain.delivery.repository.DeliveryDriverRepository;
import com.trinusphere.beebite.domain.delivery.valueobject.DriverStatus;
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
