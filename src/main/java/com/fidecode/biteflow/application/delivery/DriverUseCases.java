package com.fidecode.biteflow.application.delivery;

import com.fidecode.biteflow.application.delivery.dto.DriverRequest;
import com.fidecode.biteflow.application.delivery.dto.DriverResponse;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.exception.DriverNotFoundException;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.delivery.valueobject.DriverStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DriverUseCases {

    private final DeliveryDriverRepository driverRepository;

    @Inject
    public DriverUseCases(DeliveryDriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public DriverResponse create(UUID storeId, DriverRequest request) {
        DeliveryDriver driver = new DeliveryDriver(storeId, request.name(), request.phone());

        if (request.status() != null) driver.setStatus(DriverStatus.valueOf(request.status()));
        if (request.isFixed() != null) driver.setFixed(request.isFixed());
        if (request.isDaily() != null) driver.setDaily(request.isDaily());
        if (request.allowance() != null) driver.setAllowance(request.allowance());
        if (request.hasFixedDeliveryValue() != null) driver.setHasFixedDeliveryValue(request.hasFixedDeliveryValue());
        if (request.fixedDeliveryValue() != null) driver.setFixedDeliveryValue(request.fixedDeliveryValue());

        DeliveryDriver saved = driverRepository.save(driver);
        return toResponse(saved);
    }

    public DriverResponse update(UUID id, DriverRequest request) {
        DeliveryDriver driver = driverRepository.findById(id)
            .orElseThrow(() -> new DriverNotFoundException(id.toString()));

        driver.setName(request.name());
        if (request.phone() != null) driver.setPhone(request.phone());
        if (request.status() != null) driver.setStatus(DriverStatus.valueOf(request.status()));
        if (request.isFixed() != null) driver.setFixed(request.isFixed());
        if (request.isDaily() != null) driver.setDaily(request.isDaily());
        if (request.allowance() != null) driver.setAllowance(request.allowance());
        if (request.hasFixedDeliveryValue() != null) driver.setHasFixedDeliveryValue(request.hasFixedDeliveryValue());
        if (request.fixedDeliveryValue() != null) driver.setFixedDeliveryValue(request.fixedDeliveryValue());

        DeliveryDriver saved = driverRepository.save(driver);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        driverRepository.findById(id)
            .orElseThrow(() -> new DriverNotFoundException(id.toString()));
        driverRepository.deleteById(id);
    }

    public List<DriverResponse> list(UUID storeId) {
        return driverRepository.findAllByStoreId(storeId).stream()
            .map(DriverUseCases::toResponse).toList();
    }

    public DriverResponse getById(UUID id) {
        return driverRepository.findById(id)
            .map(DriverUseCases::toResponse)
            .orElseThrow(() -> new DriverNotFoundException(id.toString()));
    }

    public static DriverResponse toResponse(DeliveryDriver d) {
        return new DriverResponse(
            d.getId() != null ? d.getId().toString() : null,
            d.getName(),
            d.getPhone(),
            d.getStatus().name(),
            d.isFixed(),
            d.isDaily(),
            d.getAllowance(),
            d.isHasFixedDeliveryValue(),
            d.getFixedDeliveryValue(),
            d.getTotalDeliveries(),
            d.getRating(),
            d.getCreatedAt() != null ? d.getCreatedAt().toString() : null
        );
    }
}
