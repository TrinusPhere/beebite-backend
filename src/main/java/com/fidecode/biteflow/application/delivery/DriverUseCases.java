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

    public DriverResponse update(UUID storeId, UUID id, DriverRequest request) {
        DeliveryDriver driver = driverRepository.findById(id)
            .orElseThrow(() -> new DriverNotFoundException(id.toString()));
        if (!driver.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");

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

    public void delete(UUID storeId, UUID id) {
        DeliveryDriver driver = driverRepository.findById(id)
            .orElseThrow(() -> new DriverNotFoundException(id.toString()));
        if (!driver.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        driverRepository.deleteById(id);
    }

    public com.fidecode.biteflow.application.shared.PageResponse<DriverResponse> list(UUID storeId, int page, int size) {
        com.fidecode.biteflow.domain.shared.PageResult<DeliveryDriver> result = driverRepository.findAllByStoreId(storeId, page, size);
        List<DriverResponse> content = result.getContent().stream()
            .map(DriverUseCases::toResponse).toList();
        return new com.fidecode.biteflow.application.shared.PageResponse<>(content, result.getPage(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public DriverResponse getById(UUID storeId, UUID id) {
        DeliveryDriver driver = driverRepository.findById(id)
            .orElseThrow(() -> new DriverNotFoundException(id.toString()));
        if (!driver.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        return toResponse(driver);
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
