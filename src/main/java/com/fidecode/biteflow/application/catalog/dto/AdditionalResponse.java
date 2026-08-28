package com.fidecode.biteflow.application.catalog.dto;

import java.math.BigDecimal;

public record AdditionalResponse(String id, String name, BigDecimal price, String category, boolean active) {
}
