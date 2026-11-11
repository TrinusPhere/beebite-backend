package com.fidecode.biteflow.infrastructure.config;

import com.fidecode.biteflow.application.sync.dto.EdgeSyncBatchRequest;
import com.fidecode.biteflow.application.sync.dto.EdgeSyncBatchResponse;
import com.fidecode.biteflow.application.sync.dto.EdgeSyncEventDto;
import com.fidecode.biteflow.domain.printing.valueobject.ReceiptSections;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Registra classes e records serializados/desserializados manualmente via Jackson ObjectMapper
 * fora de endpoints JAX-RS diretos (persistência JSON em banco, HTTP client de Edge Sync, etc.),
 * garantindo funcionamento 100% em imagem nativa GraalVM/Mandrel.
 */
@RegisterForReflection(targets = {
        ReceiptSections.class,
        EdgeSyncBatchRequest.class,
        EdgeSyncBatchResponse.class,
        EdgeSyncEventDto.class
})
public class NativeReflectionConfig {
}

