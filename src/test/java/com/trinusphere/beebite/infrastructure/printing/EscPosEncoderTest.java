package com.trinusphere.beebite.infrastructure.printing;

import static org.junit.jupiter.api.Assertions.*;

import com.trinusphere.beebite.domain.printing.entity.Printer;
import com.trinusphere.beebite.domain.printing.receipt.ReceiptDocument;
import com.trinusphere.beebite.domain.printing.receipt.ReceiptElement;
import com.trinusphere.beebite.domain.printing.receipt.ReceiptElement.Align;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EscPosEncoderTest {

    private final EscPosEncoderImpl encoder = new EscPosEncoderImpl();
    private final UUID storeId = UUID.randomUUID();

    @Test
    void iniciaComComandoResetECodePage() {
        var printer = new Printer(storeId, "Epson", "NETWORK");
        printer.configureHardware(80, 9100, "CP860", true);
        var doc = new ReceiptDocument(List.of(new ReceiptElement.Text("Teste", Align.LEFT, false, false)), 48);

        byte[] bytes = encoder.encode(doc, printer);

        // ESC @ = 0x1B, 0x40
        assertEquals(0x1B, bytes[0]);
        assertEquals(0x40, bytes[1]);
        // ESC t 3 (CP860) = 0x1B, 0x74, 0x03
        assertEquals(0x1B, bytes[2]);
        assertEquals(0x74, bytes[3]);
        assertEquals(0x03, bytes[4]);
    }

    @Test
    void cortaPapelQuandoHabilitado() {
        var printer = new Printer(storeId, "Epson", "NETWORK");
        printer.configureHardware(80, 9100, "CP860", true);
        var doc = new ReceiptDocument(List.of(new ReceiptElement.Text("Fim", Align.LEFT, false, false)), 48);

        byte[] bytes = encoder.encode(doc, printer);

        // GS V 66 0 = 0x1D, 0x56, 0x42, 0x00
        int len = bytes.length;
        assertEquals(0x1D, bytes[len - 4]);
        assertEquals(0x56, bytes[len - 3]);
        assertEquals(0x42, bytes[len - 2]);
        assertEquals(0x00, bytes[len - 1]);
    }

    @Test
    void naoCortaPapelQuandoDesabilitado() {
        var printer = new Printer(storeId, "Epson", "NETWORK");
        printer.configureHardware(80, 9100, "CP860", false);
        var doc = new ReceiptDocument(List.of(new ReceiptElement.Text("Fim", Align.LEFT, false, false)), 48);

        byte[] bytes = encoder.encode(doc, printer);

        int len = bytes.length;
        // Não termina com GS V
        assertNotEquals(0x56, bytes[len - 3]);
    }

    @Test
    void aplicaFormatacaoNegritoETamanhoDuplo() {
        var printer = new Printer(storeId, "Epson", "NETWORK");
        var doc = new ReceiptDocument(List.of(new ReceiptElement.Text("TITULO", Align.CENTER, true, true)), 48);

        byte[] bytes = encoder.encode(doc, printer);
        String hex = toHex(bytes);

        // Verifica que contém GS ! 0x11 (double size) e ESC E 1 (bold)
        assertTrue(hex.contains("1d 21 11"));
        assertTrue(hex.contains("1b 45 01"));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString();
    }
}
