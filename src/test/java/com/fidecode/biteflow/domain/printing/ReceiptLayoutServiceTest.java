package com.fidecode.biteflow.domain.printing;

import static org.junit.jupiter.api.Assertions.*;

import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.entity.OrderItemModifier;
import com.fidecode.biteflow.domain.order.entity.OrderItemRemoval;
import com.fidecode.biteflow.domain.order.valueobject.OrderType;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import com.fidecode.biteflow.domain.printing.entity.PrintTemplate;
import com.fidecode.biteflow.domain.printing.receipt.ReceiptDocument;
import com.fidecode.biteflow.domain.printing.service.ReceiptLayoutService;
import com.fidecode.biteflow.domain.printing.valueobject.DocumentType;
import com.fidecode.biteflow.domain.printing.valueobject.PaperWidth;
import com.fidecode.biteflow.domain.printing.valueobject.ReceiptSections;
import com.fidecode.biteflow.domain.store.entity.Store;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReceiptLayoutServiceTest {

    private final ReceiptLayoutService service = new ReceiptLayoutService();
    private final UUID storeId = UUID.randomUUID();
    private Order order;
    private Store store;

    @BeforeEach
    void setUp() {
        store = Store.reconstitute(storeId, "BiteFlow Lanchonete", null, true, null, "Rua A, 123 - Centro", null);
        order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.DINHEIRO);
        order.setDisplayId("0042");
        order.updateCustomerInfo("Maria Silva", "Rua das Flores, 10");
        order.updateCustomerContact("11999998888", "Apto 2", "Perto da praça");
        order.defineCashChangeFor(new BigDecimal("100.00"));
        order.updateNotes("Sem pressa");
        order.changeDeliveryCost(new BigDecimal("5.00"));

        var item = new OrderItem(UUID.randomUUID(), "X-Bacon", 2, new BigDecimal("25.00"), "bem passado");
        item.addModifier(new OrderItemModifier(UUID.randomUUID(), "Extras", UUID.randomUUID(), "Queijo extra",
                new BigDecimal("3.00")));
        item.addRemoval(new OrderItemRemoval(UUID.randomUUID(), "Cebola"));
        order.addItem(item);
    }

    private String text(PrintTemplate template, PaperWidth width) {
        ReceiptDocument doc = service.build(order, store, template, width);
        return String.join("\n", doc.render().stream().map(l -> l.text()).toList());
    }

    @Test
    void cupomDoClienteContemLojaClienteItensTotaisEPagamento() {
        var out = text(PrintTemplate.defaultFor(storeId, DocumentType.CUSTOMER_RECEIPT), PaperWidth.MM80);
        assertTrue(out.contains("BiteFlow Lanchonete"));
        assertTrue(out.contains("Rua A, 123 - Centro"));
        assertTrue(out.contains("0042"));
        assertTrue(out.contains("Maria Silva"));
        assertTrue(out.contains("11999998888"));
        assertTrue(out.contains("Rua das Flores, 10"));
        assertTrue(out.contains("2x X-Bacon"));
        assertTrue(out.contains("Queijo extra"));
        assertTrue(out.contains("Cebola"));
        assertTrue(out.contains("bem passado"));
        assertTrue(out.contains("R$ 5,00"));
        assertTrue(out.contains("R$ 61,00"));
        assertTrue(out.contains("Dinheiro"));
        assertTrue(out.contains("R$ 100,00"));
        assertTrue(out.contains("Sem pressa"));
    }

    @Test
    void secaoDesligadaNaoApareceNaImpressao() {
        var t = PrintTemplate.defaultFor(storeId, DocumentType.CUSTOMER_RECEIPT);
        t.update(ReceiptSections.none(), null, null, false);
        var out = text(t, PaperWidth.MM80);
        assertFalse(out.contains("Maria Silva"));
        assertFalse(out.contains("Dinheiro"));
        assertFalse(out.contains("R$ 61,00"));
        assertFalse(out.contains("BiteFlow Lanchonete"));
    }

    @Test
    void cozinhaPorPadraoNaoMostraValoresNemPagamento() {
        var out = text(PrintTemplate.defaultFor(storeId, DocumentType.KITCHEN), PaperWidth.MM80);
        assertTrue(out.contains("2x X-Bacon"));
        assertTrue(out.contains("Cebola"));
        assertFalse(out.contains("R$"));
        assertFalse(out.contains("Dinheiro"));
    }

    @Test
    void cabecalhoERodapeDoModeloSaoImpressos() {
        var t = PrintTemplate.defaultFor(storeId, DocumentType.CUSTOMER_RECEIPT);
        t.update(t.getSections(), "PROMO DA SEMANA", "Volte sempre!", true);
        var out = text(t, PaperWidth.MM58);
        assertTrue(out.contains("PROMO DA SEMANA"));
        assertTrue(out.contains("Volte sempre!"));
    }

    @Test
    void nenhumaLinhaUltrapassaALarguraDaBobina() {
        for (var w : List.of(PaperWidth.MM58, PaperWidth.MM80)) {
            var doc = service.build(order, store, PrintTemplate.defaultFor(storeId, DocumentType.CUSTOMER_RECEIPT), w);
            doc.render().forEach(l -> assertTrue(l.text().length() <= w.columns(),
                    "excedeu " + w.columns() + ": '" + l.text() + "'"));
        }
    }

    @Test
    void modeloDeTesteNomeiaAImpressoraELarguraDoPapel() {
        var doc = service.buildTestPage("Epson Caixa", store.getName(), PaperWidth.MM58);
        var out = String.join("\n", doc.render().stream().map(l -> l.text()).toList());
        assertTrue(out.contains("Epson Caixa"));
        assertTrue(out.contains("58"));
        assertEquals(32, doc.columns());
    }
}

