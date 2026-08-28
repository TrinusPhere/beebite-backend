package com.fidecode.biteflow.application.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

public record ModifierGroupResponse(String id, String name, String type, boolean required, int minSelections,
        int maxSelections, String icon, List<ModifierOptionResponse> options) {
    public record ModifierOptionResponse(String id, String name, BigDecimal price) {
    }
}
