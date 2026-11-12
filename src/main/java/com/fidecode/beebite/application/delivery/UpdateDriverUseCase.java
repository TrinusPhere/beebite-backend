package com.fidecode.beebite.application.delivery;

import com.fidecode.beebite.application.delivery.dto.DriverRequest;
import com.fidecode.beebite.application.delivery.dto.DriverResponse;
import com.fidecode.beebite.domain.delivery.entity.DeliveryDriver;
import com.fidecode.beebite.domain.delivery.exception.DriverNotFoundException;
import com.fidecode.beebite.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.beebite.domain.delivery.valueobject.DriverStatus;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateDriverUseCase {

    private final DeliveryDriverRepository repository;

    @Inject
    public UpdateDriverUseCase(DeliveryDriverRepository repository) {
        this.repository = repository;
    }

    public DriverResponse execute(UUID storeId, UUID id, DriverRequest request) {
        DeliveryDriver driver = repository.findById(id).orElseThrow(() -> new DriverNotFoundException(id.toString()));
        StoreGuard.enforce(driver, storeId);

        driver.updateProfile(request.name(), request.phone());
        driver.configureWork(request.status() != null ? DriverStatus.valueOf(request.status()) : null,
                request.isFixed(), request.isDaily(), request.allowance(), request.hasFixedDeliveryValue(),
                request.fixedDeliveryValue());

        return DriverDtoMapper.toResponse(repository.save(driver));
    }
}
