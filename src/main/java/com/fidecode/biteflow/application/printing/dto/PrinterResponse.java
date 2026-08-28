package com.fidecode.biteflow.application.printing.dto;

public record PrinterResponse(String id, String name, String connectionType, String address, boolean active) {
}
