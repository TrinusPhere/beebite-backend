package com.fidecode.biteflow.application.store.dto;

public record StoreInfoResponse(
    String id,
    String name,
    String logoUrl,
    boolean isOpen,
    String openingHours,
    String address,
    String publicSlug
) {}
