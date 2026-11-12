package com.fidecode.beebite.application.delivery;

import com.fidecode.beebite.application.delivery.dto.DriverResponse;
import com.fidecode.beebite.application.shared.PageResponse;
import com.fidecode.beebite.domain.delivery.entity.DeliveryDriver;
import com.fidecode.beebite.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.beebite.domain.shared.PageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ListDriversUseCase {

    private final DeliveryDriverRepository repository;

    @Inject
    public ListDriversUseCase(DeliveryDriverRepository repository) {
        this.repository = repository;
    }

    public PageResponse<DriverResponse> execute(UUID storeId, int page, int size) {
        PageResult<DeliveryDriver> result = repository.findAllByStoreId(storeId, page, size);
        List<DriverResponse> content = result.getContent().stream().map(DriverDtoMapper::toResponse).toList();
        return new PageResponse<>(content, result.getPage(), result.getSize(), result.getTotalElements(),
                result.getTotalPages());
    }
}
