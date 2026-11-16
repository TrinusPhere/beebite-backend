package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.AdditionalResponse;
import com.trinusphere.beebite.application.shared.PageResponse;
import com.trinusphere.beebite.domain.catalog.entity.Additional;
import com.trinusphere.beebite.domain.catalog.repository.AdditionalRepository;
import com.trinusphere.beebite.domain.shared.PageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ListAdditionalsUseCase {

    private final AdditionalRepository repository;

    @Inject
    public ListAdditionalsUseCase(AdditionalRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AdditionalResponse> execute(UUID storeId, int page, int size) {
        PageResult<Additional> result = repository.findAllByStoreId(storeId, page, size);
        List<AdditionalResponse> content = result.getContent().stream().map(AdditionalDtoMapper::toResponse).toList();
        return new PageResponse<>(content, result.getPage(), result.getSize(), result.getTotalElements(),
                result.getTotalPages());
    }
}
