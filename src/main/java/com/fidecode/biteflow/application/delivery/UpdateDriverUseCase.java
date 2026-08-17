package com.fidecode.biteflow.application.delivery;

import com.fidecode.biteflow.application.delivery.dto.DriverRequest;
import com.fidecode.biteflow.application.delivery.dto.DriverResponse;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.exception.DriverNotFoundException;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.delivery.valueobject.DriverStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;
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
        DeliveryDriver driver = repository.findById(id)
            .orElseThrow(() -> new DriverNotFoundException(id.toString()));
        StoreGuard.enforce(driver, storeId);

        driver.setName(request.name());
        if (request.phone() != null) driver.setPhone(request.phone());
        if (request.status() != null) driver.setStatus(DriverStatus.valueOf(request.status()));
        if (request.isFixed() != null) driver.setFixed(request.isFixed());
        if (request.isDaily() != null) driver.setDaily(request.isDaily());
        if (request.allowance() != null) driver.setAllowance(request.allowance());
        if (request.hasFixedDeliveryValue() != null) driver.setHasFixedDeliveryValue(request.hasFixedDeliveryValue());
        if (request.fixedDeliveryValue() != null) driver.setFixedDeliveryValue(request.fixedDeliveryValue());

        return DriverDtoMapper.toResponse(repository.save(driver));
    }
}
