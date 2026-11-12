package com.fidecode.beebite.domain.printing;

import static org.junit.jupiter.api.Assertions.*;

import com.fidecode.beebite.domain.printing.entity.PrintTemplate;
import com.fidecode.beebite.domain.printing.valueobject.DocumentType;
import com.fidecode.beebite.domain.printing.valueobject.PaperWidth;
import com.fidecode.beebite.domain.printing.valueobject.ReceiptSections;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PrintTemplateAndPaperTest {

    private static final UUID STORE = UUID.randomUUID();

    @Test
    void larguraDeBobinaMapeiaParaColunas() {
        assertEquals(32, PaperWidth.fromMillimeters(58).columns());
        assertEquals(48, PaperWidth.fromMillimeters(80).columns());
    }

    @Test
    void larguraDeBobinaInvalidaEhRejeitada() {
        assertThrows(IllegalArgumentException.class, () -> PaperWidth.fromMillimeters(70));
    }

    @Test
    void tipoDeDocumentoEhResolvidoPeloNome() {
        assertEquals(DocumentType.KITCHEN, DocumentType.fromValue("kitchen"));
        assertThrows(IllegalArgumentException.class, () -> DocumentType.fromValue("XYZ"));
    }

    @Test
    void modeloPadraoDaCozinhaNaoMostraPrecosNemPagamento() {
        var t = PrintTemplate.defaultFor(STORE, DocumentType.KITCHEN);
        assertFalse(t.getSections().showItemPrices());
        assertFalse(t.getSections().showPaymentMethod());
        assertFalse(t.getSections().showTotal());
        assertTrue(t.getSections().showItemRemovals());
        assertTrue(t.getSections().showItemModifiers());
    }

    @Test
    void modeloPadraoDoCupomDoClienteMostraValoresEPagamento() {
        var t = PrintTemplate.defaultFor(STORE, DocumentType.CUSTOMER_RECEIPT);
        assertTrue(t.getSections().showItemPrices());
        assertTrue(t.getSections().showTotal());
        assertTrue(t.getSections().showPaymentMethod());
        assertTrue(t.getSections().showStoreName());
    }

    @Test
    void modeloExigeStoreETipo() {
        assertThrows(IllegalArgumentException.class, () -> PrintTemplate.defaultFor(null, DocumentType.KITCHEN));
        assertThrows(IllegalArgumentException.class, () -> PrintTemplate.defaultFor(STORE, null));
    }

    @Test
    void atualizacaoSubstituiSecoesETextos() {
        var t = PrintTemplate.defaultFor(STORE, DocumentType.EXPEDITION);
        t.update(ReceiptSections.none(), "  Cabeçalho  ", "Obrigado!", false);
        assertEquals("Cabeçalho", t.getHeaderText());
        assertEquals("Obrigado!", t.getFooterText());
        assertFalse(t.isLargeTitle());
        assertFalse(t.getSections().showTotal());
    }

    @Test
    void textosMuitoLongosSaoRejeitados() {
        var t = PrintTemplate.defaultFor(STORE, DocumentType.KITCHEN);
        var longo = "x".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> t.update(ReceiptSections.none(), longo, null, true));
        assertThrows(IllegalArgumentException.class, () -> t.update(ReceiptSections.none(), null, longo, true));
    }
}

