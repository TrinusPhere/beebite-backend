package com.fidecode.biteflow.domain.printing.service;

import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.valueobject.OrderType;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import com.fidecode.biteflow.domain.printing.entity.PrintTemplate;
import com.fidecode.biteflow.domain.printing.receipt.ReceiptDocument;
import com.fidecode.biteflow.domain.printing.receipt.ReceiptElement;
import com.fidecode.biteflow.domain.printing.receipt.ReceiptElement.Align;
import com.fidecode.biteflow.domain.printing.valueobject.DocumentType;
import com.fidecode.biteflow.domain.printing.valueobject.PaperWidth;
import com.fidecode.biteflow.domain.printing.valueobject.ReceiptSections;
import com.fidecode.biteflow.domain.store.entity.Store;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Monta o {@link ReceiptDocument} de um pedido conforme o modelo configurado e a largura da bobina. */
@ApplicationScoped
public class ReceiptLayoutService {
    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReceiptDocument build(Order order, Store store, PrintTemplate template, PaperWidth width) {
        ReceiptSections s = template.getSections();
        List<ReceiptElement> e = new ArrayList<>();
        boolean large = template.isLargeTitle();

        if (template.getHeaderText() != null)
            e.add(center(template.getHeaderText(), true, false));
        if (s.showStoreName() && store != null)
            e.add(center(store.getName(), true, large));
        if (s.showStoreAddress() && store != null && notBlank(store.getAddress()))
            e.add(center(store.getAddress(), false, false));
        if (!e.isEmpty())
            e.add(new ReceiptElement.Separator('-'));

        e.add(center(title(template.getDocumentType()), true, false));
        if (s.showOrderNumber())
            e.add(center("PEDIDO #" + (notBlank(order.getDisplayId()) ? order.getDisplayId() : "-"), true, large));
        if (s.showDateTime())
            e.add(left("Data: " + DATE_TIME.format(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now())));
        if (s.showChannel() && notBlank(order.getChannel()))
            e.add(left("Canal: " + order.getChannel()));
        if (s.showServiceType())
            e.add(left("Tipo: " + serviceLabel(order.getType())));

        addCustomer(order, s, e);
        addItems(order, s, e);
        addTotals(order, s, e);
        addPayment(order, s, e);

        if (s.showOrderNotes() && notBlank(order.getNotes())) {
            e.add(new ReceiptElement.Separator('-'));
            e.add(left("Obs: " + order.getNotes()));
        }
        if (template.getFooterText() != null) {
            e.add(new ReceiptElement.Separator('-'));
            e.add(center(template.getFooterText(), false, false));
        }
        e.add(new ReceiptElement.Blank());
        return new ReceiptDocument(e, width.columns());
    }

    public ReceiptDocument buildTestPage(String printerName, String storeName, PaperWidth width) {
        List<ReceiptElement> e = new ArrayList<>();
        e.add(center("TESTE DE IMPRESSAO", true, true));
        e.add(new ReceiptElement.Separator('='));
        if (notBlank(storeName))
            e.add(center(storeName, true, false));
        e.add(left("Impressora: " + printerName));
        e.add(left("Bobina: " + width.millimeters() + "mm (" + width.columns() + " colunas)"));
        e.add(left("Data: " + DATE_TIME.format(LocalDateTime.now())));
        e.add(new ReceiptElement.Separator('-'));
        e.add(new ReceiptElement.Columns("Texto a esquerda", "R$ 12,50", false));
        e.add(left("Acentos: ação, coração, é, ü, ç"));
        e.add(new ReceiptElement.Separator('='));
        e.add(center("Impressao OK", true, false));
        e.add(new ReceiptElement.Blank());
        return new ReceiptDocument(e, width.columns());
    }

    private void addCustomer(Order order, ReceiptSections s, List<ReceiptElement> e) {
        List<ReceiptElement> block = new ArrayList<>();
        if (s.showCustomerName() && notBlank(order.getCustomerName()))
            block.add(left("Cliente: " + order.getCustomerName()));
        if (s.showCustomerPhone() && notBlank(order.getCustomerPhone()))
            block.add(left("Tel: " + order.getCustomerPhone()));
        if (s.showCustomerAddress() && notBlank(order.getCustomerAddress())) {
            block.add(left("End: " + order.getCustomerAddress()));
            if (notBlank(order.getCustomerAddressComplement()))
                block.add(left("Compl: " + order.getCustomerAddressComplement()));
            if (notBlank(order.getCustomerAddressReference()))
                block.add(left("Ref: " + order.getCustomerAddressReference()));
        }
        if (!block.isEmpty()) {
            e.add(new ReceiptElement.Separator('-'));
            e.addAll(block);
        }
    }

    private void addItems(Order order, ReceiptSections s, List<ReceiptElement> e) {
        e.add(new ReceiptElement.Separator('-'));
        for (OrderItem item : order.getItems()) {
            String label = item.getQuantity() + "x " + item.getProductName();
            if (s.showItemPrices())
                e.add(new ReceiptElement.Columns(label, money(item.getTotalPrice()), true));
            else
                e.add(new ReceiptElement.Text(label, Align.LEFT, true, false));
            if (s.showItemModifiers())
                item.getModifiers().forEach(m -> e.add(left("  + " + m.getOptionName())));
            if (s.showItemRemovals())
                item.getRemovals().forEach(r -> e.add(left("  - SEM " + r.getIngredientName())));
            if (s.showItemNotes() && notBlank(item.getObservation()))
                e.add(left("  Obs: " + item.getObservation()));
        }
    }

    private void addTotals(Order order, ReceiptSections s, List<ReceiptElement> e) {
        boolean hasDelivery = order.getDeliveryCost() != null && order.getDeliveryCost().signum() > 0;
        boolean any = s.showSubtotal() || (s.showDeliveryFee() && hasDelivery) || s.showTotal();
        if (!any)
            return;
        e.add(new ReceiptElement.Separator('-'));
        if (s.showSubtotal())
            e.add(new ReceiptElement.Columns("Subtotal", money(order.getSubtotal()), false));
        if (s.showDeliveryFee() && hasDelivery)
            e.add(new ReceiptElement.Columns("Taxa de entrega", money(order.getDeliveryCost()), false));
        if (s.showTotal())
            e.add(new ReceiptElement.Columns("TOTAL", money(order.getTotalValue()), true));
    }

    private void addPayment(Order order, ReceiptSections s, List<ReceiptElement> e) {
        if (s.showPaymentMethod() && order.getPaymentMethod() != null) {
            e.add(left("Pagamento: " + paymentLabel(order.getPaymentMethod())));
            if (s.showCashChange() && order.getPaymentMethod() == PaymentMethod.DINHEIRO
                    && order.getCashChangeFor() != null && order.getCashChangeFor().signum() > 0)
                e.add(left("Troco para: " + money(order.getCashChangeFor())));
        }
    }

    private static String title(DocumentType type) {
        return switch (type) {
            case KITCHEN -> "COZINHA";
            case EXPEDITION -> "EXPEDICAO";
            case CUSTOMER_RECEIPT -> "CUPOM";
        };
    }

    private static String serviceLabel(OrderType type) {
        return type == OrderType.DELIVERY ? "Entrega" : "Balcão";
    }

    private static String paymentLabel(PaymentMethod method) {
        return switch (method) {
            case PIX -> "Pix";
            case CREDITO -> "Crédito";
            case DEBITO -> "Débito";
            case DINHEIRO -> "Dinheiro";
        };
    }

    static String money(BigDecimal value) {
        NumberFormat nf = NumberFormat.getNumberInstance(PT_BR);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return "R$ " + nf.format((value != null ? value : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
    }

    private static ReceiptElement center(String text, boolean bold, boolean large) {
        return new ReceiptElement.Text(text, Align.CENTER, bold, large);
    }

    private static ReceiptElement left(String text) {
        return new ReceiptElement.Text(text, Align.LEFT, false, false);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}

