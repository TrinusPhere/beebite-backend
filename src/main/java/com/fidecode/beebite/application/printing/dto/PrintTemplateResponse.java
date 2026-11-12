package com.fidecode.beebite.application.printing.dto;

public record PrintTemplateResponse(String documentType, PrintTemplateRequest.Sections sections, String headerText,
        String footerText, boolean largeTitle) {
}

