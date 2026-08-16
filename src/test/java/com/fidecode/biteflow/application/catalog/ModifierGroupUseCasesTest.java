package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.ModifierGroupRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@QuarkusTest
public class ModifierGroupUseCasesTest {

    @Inject
    ModifierGroupUseCases useCases;

    @Test
    public void testCreateModifierGroup() {
        ModifierGroupRequest req = new ModifierGroupRequest(
            "Ponto", "RADIO", true, 1, 1, null,
            List.of(new ModifierGroupRequest.ModifierOptionDto("Mal", BigDecimal.ZERO))
        );
        useCases.create(UUID.fromString("00000000-0000-0000-0000-000000000001"), req);
    }
}
