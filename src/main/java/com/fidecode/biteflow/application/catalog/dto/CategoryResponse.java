package com.fidecode.biteflow.application.catalog.dto;

import java.time.Instant;
import java.util.List;

public record CategoryResponse(String id, String name, String status, int displayOrder,
        List<String> defaultModifierGroupIds, Instant createdAt) {
}
