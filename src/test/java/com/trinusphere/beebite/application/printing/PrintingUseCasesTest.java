package com.trinusphere.beebite.application.printing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.trinusphere.beebite.application.printing.dto.*;
import com.trinusphere.beebite.application.printing.port.EscPosEncoder;
import com.trinusphere.beebite.application.printing.port.PrinterGateway;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.entity.OrderItem;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.order.valueobject.OrderType;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import com.trinusphere.beebite.domain.printing.entity.PrintTemplate;
import com.trinusphere.beebite.domain.printing.entity.Printer;
import com.trinusphere.beebite.domain.printing.exception.PrinterConnectionException;
import com.trinusphere.beebite.domain.printing.repository.PrintTemplateRepository;
import com.trinusphere.beebite.domain.printing.repository.PrinterRepository;
import com.trinusphere.beebite.domain.printing.service.ReceiptLayoutService;
import com.trinusphere.beebite.domain.printing.valueobject.DocumentType;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrintingUseCasesTest {

    @Mock
    OrderRepository orders;
    @Mock
    com.trinusphere.beebite.domain.store.repository.StoreRepository stores;
    @Mock
    PrinterRepository printers;
    @Mock
    PrintTemplateRepository templates;
    @Mock
    EscPosEncoder encoder;
    @Mock
    PrinterGateway gateway;

    private final ReceiptLayoutService layout = new ReceiptLayoutService();
    private final UUID storeId = UUID.randomUUID();
    private final byte[] bytes = new byte[] { 0x1B, 0x40, 'O', 'K' };
    private DispatchReceiptService dispatch;
    private Store store;
    private Order order;

    @BeforeEach
    void setUp() {
        dispatch = new DispatchReceiptService(encoder, gateway);
        store = Store.reconstitute(storeId, "Loja", null, true, null, "Rua A", null);
        order = new Order(storeId, OrderType.BALCAO, PaymentMethod.PIX);
        order.setDisplayId("7");
        order.addItem(new OrderItem(UUID.randomUUID(), "X-Burger", 1, new BigDecimal("20.00"), null));
    }

    private Printer printer(String type, boolean active) {
        var p = new Printer(storeId, "Epson", type);
        p.setId(UUID.randomUUID());
        p.configureAddress("192.168.0.50");
        p.changeActive(active);
        return p;
    }

    private PrintOrderUseCase printOrder() {
        return new PrintOrderUseCase(orders, stores, printers, templates, layout, dispatch);
    }

    @Test
    void impressaoPorRedeEnviaOsBytesPeloGateway() {
        var p = printer("NETWORK", true);
        var id = UUID.randomUUID();
        when(orders.findById(id)).thenReturn(Optional.of(order));
        when(stores.findById(storeId)).thenReturn(Optional.of(store));
        when(printers.findById(p.getId())).thenReturn(Optional.of(p));
        when(templates.findByStoreIdAndDocumentType(storeId, DocumentType.KITCHEN)).thenReturn(Optional.empty());
        when(encoder.encode(any(), eq(p))).thenReturn(bytes);

        var result = printOrder().execute(storeId, id, new PrintOrderRequest(p.getId(), "KITCHEN"));

        assertEquals("SENT", result.mode());
        assertTrue(result.sent());
        verify(gateway).send(p, bytes);
    }

    @Test
    void impressoraUsbDevolveBytesEmBase64SemUsarOGateway() {
        var p = printer("USB_BROWSER", true);
        var id = UUID.randomUUID();
        when(orders.findById(id)).thenReturn(Optional.of(order));
        when(stores.findById(storeId)).thenReturn(Optional.of(store));
        when(printers.findById(p.getId())).thenReturn(Optional.of(p));
        when(templates.findByStoreIdAndDocumentType(storeId, DocumentType.CUSTOMER_RECEIPT))
                .thenReturn(Optional.empty());
        when(encoder.encode(any(), eq(p))).thenReturn(bytes);

        var result = printOrder().execute(storeId, id, new PrintOrderRequest(p.getId(), "CUSTOMER_RECEIPT"));

        assertEquals("BROWSER", result.mode());
        assertFalse(result.sent());
        assertArrayEquals(bytes, Base64.getDecoder().decode(result.escPosBase64()));
        verifyNoInteractions(gateway);
    }

    @Test
    void impressoraInativaEhRejeitada() {
        var p = printer("NETWORK", false);
        var id = UUID.randomUUID();
        when(orders.findById(id)).thenReturn(Optional.of(order));
        when(stores.findById(storeId)).thenReturn(Optional.of(store));
        when(printers.findById(p.getId())).thenReturn(Optional.of(p));
        when(templates.findByStoreIdAndDocumentType(storeId, DocumentType.KITCHEN)).thenReturn(Optional.empty());

        assertThrows(PrinterConnectionException.class,
                () -> printOrder().execute(storeId, id, new PrintOrderRequest(p.getId(), "KITCHEN")));
        verifyNoInteractions(gateway);
    }

    @Test
    void tipoDeConexaoNaoSuportadoPeloBackendEhRejeitado() {
        var p = printer("LOCAL_AGENT", true);
        var doc = layout.buildTestPage("x", "y", p.getPaperWidth());

        assertThrows(PrinterConnectionException.class, () -> dispatch.dispatch(p, doc));
        verifyNoInteractions(gateway);
    }

    @Test
    void pedidoDeOutraLojaEhNegado() {
        var id = UUID.randomUUID();
        var outra = new Order(UUID.randomUUID(), OrderType.BALCAO, PaymentMethod.PIX);
        when(orders.findById(id)).thenReturn(Optional.of(outra));

        assertThrows(ForbiddenAccessException.class,
                () -> printOrder().execute(storeId, id, new PrintOrderRequest(UUID.randomUUID(), "KITCHEN")));
        verifyNoInteractions(gateway);
    }

    @Test
    void impressoraDeOutraLojaEhNegada() {
        var id = UUID.randomUUID();
        var alheia = new Printer(UUID.randomUUID(), "Alheia", "NETWORK");
        alheia.setId(UUID.randomUUID());
        when(orders.findById(id)).thenReturn(Optional.of(order));
        when(printers.findById(alheia.getId())).thenReturn(Optional.of(alheia));

        assertThrows(ForbiddenAccessException.class,
                () -> printOrder().execute(storeId, id, new PrintOrderRequest(alheia.getId(), "KITCHEN")));
        verifyNoInteractions(gateway);
    }

    @Test
    void testeDeImpressoraEnviaPaginaDeTesteReal() {
        var p = printer("NETWORK", true);
        when(printers.findById(p.getId())).thenReturn(Optional.of(p));
        when(stores.findById(storeId)).thenReturn(Optional.of(store));
        when(encoder.encode(any(), eq(p))).thenReturn(bytes);

        var result = new TestPrinterUseCase(printers, stores, layout, dispatch).execute(storeId, p.getId());

        assertTrue(result.sent());
        verify(gateway).send(p, bytes);
    }

    @Test
    void obterModeloSemRegistroDevolvePadraoDoTipo() {
        when(templates.findByStoreIdAndDocumentType(storeId, DocumentType.KITCHEN)).thenReturn(Optional.empty());

        var r = new GetPrintTemplateUseCase(templates).execute(storeId, "kitchen");

        assertEquals("KITCHEN", r.documentType());
        assertFalse(r.sections().showItemPrices());
    }

    @Test
    void atualizarModeloCriaQuandoNaoExisteEPersiste() {
        when(templates.findByStoreIdAndDocumentType(storeId, DocumentType.EXPEDITION)).thenReturn(Optional.empty());
        when(templates.save(any())).thenAnswer(i -> i.getArgument(0));
        var sections = PrintingDtoMapper.toSectionsDto(
                com.trinusphere.beebite.domain.printing.valueobject.ReceiptSections.none());

        var r = new UpdatePrintTemplateUseCase(templates).execute(storeId, "EXPEDITION",
                new PrintTemplateRequest(sections, " Topo ", "Fim", false));

        assertEquals("Topo", r.headerText());
        assertFalse(r.largeTitle());
        verify(templates).save(any(PrintTemplate.class));
    }

    @Test
    void previewUsaModeloEnviadoSemSalvar() {
        when(stores.findById(storeId)).thenReturn(Optional.of(store));
        var sections = PrintingDtoMapper.toSectionsDto(
                com.trinusphere.beebite.domain.printing.valueobject.ReceiptSections.none());
        var req = new PreviewReceiptRequest(new PrintTemplateRequest(sections, "TOPO DE TESTE", null, false), 58);

        var r = new PreviewReceiptUseCase(stores, templates, layout).execute(storeId, "CUSTOMER_RECEIPT", req);

        assertEquals(32, r.columns());
        assertEquals(58, r.paperWidthMm());
        assertTrue(r.lines().stream().anyMatch(l -> l.text().contains("TOPO DE TESTE")));
        verify(templates, never()).save(any());
        verify(templates, never()).findByStoreIdAndDocumentType(any(), any());
    }

    @Test
    void previewSemModeloUsaOSalvo() {
        when(stores.findById(storeId)).thenReturn(Optional.of(store));
        var saved = PrintTemplate.defaultFor(storeId, DocumentType.CUSTOMER_RECEIPT);
        when(templates.findByStoreIdAndDocumentType(storeId, DocumentType.CUSTOMER_RECEIPT))
                .thenReturn(Optional.of(saved));

        var r = new PreviewReceiptUseCase(stores, templates, layout).execute(storeId, "CUSTOMER_RECEIPT",
                new PreviewReceiptRequest(null, 80));

        assertEquals(48, r.columns());
        assertTrue(r.lines().stream().anyMatch(l -> l.text().contains("Loja")));
    }
}
