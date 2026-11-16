package com.trinusphere.beebite.application.printing.dto;

public record PrinterResponse(String id, String name, String connectionType, String address, boolean active,
        int paperWidthMm, int columns, int port, String codePage, boolean cutPaper) {
}
