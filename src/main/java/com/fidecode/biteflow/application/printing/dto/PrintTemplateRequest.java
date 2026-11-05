package com.fidecode.biteflow.application.printing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Modelo de comanda enviado pelo painel (salvar ou pré-visualizar). */
public record PrintTemplateRequest(@NotNull @Valid Sections sections, @Size(max = 255) String headerText,
        @Size(max = 255) String footerText, Boolean largeTitle) {

    public record Sections(boolean showStoreName, boolean showStoreAddress, boolean showCustomerName,
            boolean showCustomerPhone, boolean showCustomerAddress, boolean showOrderNumber, boolean showDateTime,
            boolean showChannel, boolean showServiceType, boolean showOrderNotes, boolean showItemModifiers,
            boolean showItemRemovals, boolean showItemNotes, boolean showItemPrices, boolean showSubtotal,
            boolean showDeliveryFee, boolean showTotal, boolean showPaymentMethod, boolean showCashChange) {
    }
}

