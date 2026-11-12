package com.fidecode.beebite.domain.printing;

import static org.junit.jupiter.api.Assertions.*;

import com.fidecode.beebite.domain.printing.receipt.ReceiptDocument;
import com.fidecode.beebite.domain.printing.receipt.ReceiptDocument.RenderedLine;
import com.fidecode.beebite.domain.printing.receipt.ReceiptElement;
import com.fidecode.beebite.domain.printing.receipt.ReceiptElement.Align;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReceiptDocumentTest {

    private static List<RenderedLine> render(int columns, ReceiptElement... elements) {
        return new ReceiptDocument(List.of(elements), columns).render();
    }

    @Test
    void textoCentralizadoOcupaALarguraDaBobina() {
        var lines = render(32, new ReceiptElement.Text("ABC", Align.CENTER, false, false));
        assertEquals(1, lines.size());
        assertEquals(" ".repeat(14) + "ABC", lines.get(0).text());
    }

    @Test
    void textoAlinhadoADireita() {
        var lines = render(10, new ReceiptElement.Text("AB", Align.RIGHT, false, false));
        assertEquals("        AB", lines.get(0).text());
    }

    @Test
    void textoLongoQuebraEmPalavrasSemUltrapassarAsColunas() {
        var lines = render(10, new ReceiptElement.Text("pizza de calabresa grande", Align.LEFT, false, false));
        assertTrue(lines.size() >= 3);
        lines.forEach(l -> assertTrue(l.text().length() <= 10, "linha excedeu: '" + l.text() + "'"));
        assertEquals("pizza de", lines.get(0).text());
    }

    @Test
    void palavraMaiorQueALarguraEhQuebradaNaForca() {
        var lines = render(5, new ReceiptElement.Text("abcdefghij", Align.LEFT, false, false));
        assertEquals(List.of("abcde", "fghij"), lines.stream().map(RenderedLine::text).toList());
    }

    @Test
    void duasColunasPreencheEspacoEntreEsquerdaEDireita() {
        var lines = render(20, new ReceiptElement.Columns("2x X-Bacon", "R$ 59,80", false));
        assertEquals(1, lines.size());
        assertEquals(20, lines.get(0).text().length());
        assertTrue(lines.get(0).text().startsWith("2x X-Bacon"));
        assertTrue(lines.get(0).text().endsWith("R$ 59,80"));
    }

    @Test
    void duasColunasComEsquerdaLongaQuebraMantendoValorNaPrimeiraLinha() {
        var lines = render(24, new ReceiptElement.Columns("1x Pizza de carne de sol especial", "R$ 45,00", false));
        assertTrue(lines.size() >= 2);
        lines.forEach(l -> assertTrue(l.text().length() <= 24));
        assertTrue(lines.get(0).text().endsWith("R$ 45,00"));
    }

    @Test
    void separadorPreencheALinhaInteira() {
        var lines = render(12, new ReceiptElement.Separator('-'));
        assertEquals("-".repeat(12), lines.get(0).text());
    }

    @Test
    void textoGrandeUsaMetadeDasColunasEPreservaFlags() {
        var lines = render(32, new ReceiptElement.Text("PEDIDO 1234567890123456789", Align.CENTER, true, true));
        assertTrue(lines.size() >= 2);
        lines.forEach(l -> {
            assertTrue(l.text().length() <= 16);
            assertTrue(l.large());
            assertTrue(l.bold());
        });
    }

    @Test
    void linhaEmBrancoGeraLinhaVazia() {
        var lines = render(10, new ReceiptElement.Blank());
        assertEquals("", lines.get(0).text());
    }

    @Test
    void colunasInvalidasSaoRejeitadas() {
        assertThrows(IllegalArgumentException.class, () -> new ReceiptDocument(List.of(), 0));
    }
}

