package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.ModifierGroupResponse;
import com.trinusphere.beebite.application.shared.PageResponse;
import com.trinusphere.beebite.domain.catalog.entity.ModifierGroup;
import com.trinusphere.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.trinusphere.beebite.domain.shared.PageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ListModifierGroupsUseCase {

    private final ModifierGroupRepository repository;

    @Inject
    public ListModifierGroupsUseCase(ModifierGroupRepository repository) {
        this.repository = repository;
    }

    public PageResponse<ModifierGroupResponse> execute(UUID storeId, int page, int size) {
        PageResult<ModifierGroup> result = repository.findAllByStoreId(storeId, page, size);
        List<ModifierGroupResponse> content = result.getContent().stream().map(ModifierGroupDtoMapper::toResponse)
                .toList();
        return new PageResponse<>(content, result.getPage(), result.getSize(), result.getTotalElements(),
                result.getTotalPages());
    }
}
