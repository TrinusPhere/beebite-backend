package com.trinusphere.beebite.application.catalog.dto;

public record ProductComboComponentResponse(
        String id,
        String comboProductId,
        String componentProductId,
        String componentProductName,
        int quantity
) {}

