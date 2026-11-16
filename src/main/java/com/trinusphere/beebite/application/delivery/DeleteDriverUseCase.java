package com.trinusphere.beebite.application.delivery;

import com.trinusphere.beebite.domain.delivery.entity.DeliveryDriver;
import com.trinusphere.beebite.domain.delivery.exception.DriverNotFoundException;
import com.trinusphere.beebite.domain.delivery.repository.DeliveryDriverRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteDriverUseCase {

    private final DeliveryDriverRepository repository;

    @Inject
    public DeleteDriverUseCase(DeliveryDriverRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID storeId, UUID id) {
        DeliveryDriver driver = repository.findById(id).orElseThrow(() -> new DriverNotFoundException(id.toString()));
        StoreGuard.enforce(driver, storeId);
        repository.deleteById(id);
    }
}
