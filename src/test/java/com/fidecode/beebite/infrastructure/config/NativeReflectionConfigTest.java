package com.fidecode.beebite.infrastructure.config;

import com.fidecode.beebite.domain.printing.valueobject.ReceiptSections;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeReflectionConfigTest {

    @Test
    void shouldRegisterReceiptSectionsForNativeReflection() {
        RegisterForReflection annotation = NativeReflectionConfig.class.getAnnotation(RegisterForReflection.class);
        assertNotNull(annotation, "NativeReflectionConfig deve ter @RegisterForReflection para compilacao GraalVM Native");
        assertTrue(Arrays.asList(annotation.targets()).contains(ReceiptSections.class),
                "ReceiptSections deve estar registrado para serializacao Jackson em imagem nativa");
    }
}

