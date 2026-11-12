package com.fidecode.beebite.application.store.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StoreInfoRequest(
        String id,
        @NotBlank(message = "Nome é obrigatório") String name,
        String logoUrl,
        boolean isOpen,
        String openingHours,
        String address,
        String publicSlug) {

    public StoreInfoRequest(String name, String logoUrl, boolean isOpen, String openingHours, String address) {
        this(null, name, logoUrl, isOpen, openingHours, address, null);
    }
}
