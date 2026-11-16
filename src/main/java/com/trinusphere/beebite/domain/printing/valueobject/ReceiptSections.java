package com.trinusphere.beebite.domain.printing.valueobject;

/**
 * Seções (liga/desliga) de um modelo de comanda: dados da loja, do cliente, do pedido, itens, valores e pagamento.
 */
public record ReceiptSections(boolean showStoreName, boolean showStoreAddress, boolean showCustomerName,
        boolean showCustomerPhone, boolean showCustomerAddress, boolean showOrderNumber, boolean showDateTime,
        boolean showChannel, boolean showServiceType, boolean showOrderNotes, boolean showItemModifiers,
        boolean showItemRemovals, boolean showItemNotes, boolean showItemPrices, boolean showSubtotal,
        boolean showDeliveryFee, boolean showTotal, boolean showPaymentMethod, boolean showCashChange) {

    /** Tudo desligado. */
    public static ReceiptSections none() {
        return new ReceiptSections(false, false, false, false, false, false, false, false, false, false, false, false,
                false, false, false, false, false, false, false);
    }

    /** Modelo padrão da cozinha: foco em itens e observações, sem valores nem pagamento. */
    public static ReceiptSections kitchenDefaults() {
        return new ReceiptSections(false, false, false, false, false, true, true, true, true, true, true, true, true,
                false, false, false, false, false, false);
    }

    /** Modelo padrão da expedição: cliente, endereço e pagamento para conferência de entrega. */
    public static ReceiptSections expeditionDefaults() {
        return new ReceiptSections(false, false, true, true, true, true, true, true, true, true, false, false, true,
                false, false, true, true, true, true);
    }

    /** Modelo padrão do cupom do cliente: tudo, com valores. */
    public static ReceiptSections customerReceiptDefaults() {
        return new ReceiptSections(true, true, true, true, true, true, true, true, true, true, true, true, true, true,
                true, true, true, true, true);
    }
}

