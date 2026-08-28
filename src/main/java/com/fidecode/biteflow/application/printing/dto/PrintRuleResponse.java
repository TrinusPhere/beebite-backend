package com.fidecode.biteflow.application.printing.dto;

public record PrintRuleResponse(String id, String name, String operation, String documentType, String sectorId,
        String printerId, boolean active) {
}
