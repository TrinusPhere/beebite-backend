package com.fidecode.beebite.application.catalog;

import com.fidecode.beebite.application.catalog.dto.ModifierGroupRequest;
import com.fidecode.beebite.application.catalog.dto.ModifierGroupResponse;
import com.fidecode.beebite.domain.catalog.entity.ModifierGroup;
import com.fidecode.beebite.domain.catalog.repository.ModifierGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModifierGroupUseCasesTest {

    @Mock
    private ModifierGroupRepository modifierGroupRepository;

    private CreateModifierGroupUseCase createModifierGroupUseCase;

    private final UUID STORE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createModifierGroupUseCase = new CreateModifierGroupUseCase(modifierGroupRepository);
    }

    @Test
    void testCreateModifierGroup() {
        when(modifierGroupRepository.save(any(ModifierGroup.class))).thenAnswer(inv -> {
            ModifierGroup g = inv.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });

        ModifierGroupRequest req = new ModifierGroupRequest("Ponto", "RADIO", true, 1, 1, null, "SPECIFICATION",
                List.of(new ModifierGroupRequest.ModifierOptionDto("Mal", BigDecimal.ZERO, null)));

        ModifierGroupResponse response = createModifierGroupUseCase.execute(STORE_ID, req);

        assertNotNull(response.id());
        assertEquals("Ponto", response.name());
        verify(modifierGroupRepository).save(any(ModifierGroup.class));
    }
}
