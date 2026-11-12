package com.fidecode.beebite.domain.printing;

import static org.junit.jupiter.api.Assertions.*;

import com.fidecode.beebite.domain.printing.entity.Printer;
import com.fidecode.beebite.domain.printing.valueobject.PaperWidth;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PrinterTest {

    private static final UUID STORE = UUID.randomUUID();

    @Test
    void padroesSaoEpson80mmRedePorta9100Cp860ComCorte() {
        var p = new Printer(STORE, "Caixa", "NETWORK");
        assertEquals(PaperWidth.MM80, p.getPaperWidth());
        assertEquals(9100, p.getPort());
        assertEquals("CP860", p.getCodePage());
        assertTrue(p.isCutPaper());
        assertTrue(p.isNetwork());
    }

    @Test
    void usbPeloNavegadorNaoEhRede() {
        var p = new Printer(STORE, "USB", "USB_BROWSER");
        assertFalse(p.isNetwork());
        assertTrue(p.isBrowserDriven());
    }

    @Test
    void tipoDeConexaoInvalidoEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> new Printer(STORE, "X", "BLUETOOTH"));
    }

    @Test
    void configuracaoDeHardwareAplicaBobinaPortaCodePageECorte() {
        var p = new Printer(STORE, "Cozinha", "NETWORK");
        p.configureHardware(58, 9101, "cp850", false);
        assertEquals(PaperWidth.MM58, p.getPaperWidth());
        assertEquals(9101, p.getPort());
        assertEquals("CP850", p.getCodePage());
        assertFalse(p.isCutPaper());
    }

    @Test
    void configuracaoDeHardwareValidaEntradas() {
        var p = new Printer(STORE, "Cozinha", "NETWORK");
        assertThrows(IllegalArgumentException.class, () -> p.configureHardware(70, 9100, "CP860", true));
        assertThrows(IllegalArgumentException.class, () -> p.configureHardware(80, 0, "CP860", true));
        assertThrows(IllegalArgumentException.class, () -> p.configureHardware(80, 70000, "CP860", true));
        assertThrows(IllegalArgumentException.class, () -> p.configureHardware(80, 9100, "UTF-8", true));
    }

    @Test
    void hardwareNuloMantemOsPadroes() {
        var p = new Printer(STORE, "Cozinha", "NETWORK");
        p.configureHardware(null, null, null, null);
        assertEquals(PaperWidth.MM80, p.getPaperWidth());
        assertEquals(9100, p.getPort());
        assertEquals("CP860", p.getCodePage());
        assertTrue(p.isCutPaper());
    }
}

