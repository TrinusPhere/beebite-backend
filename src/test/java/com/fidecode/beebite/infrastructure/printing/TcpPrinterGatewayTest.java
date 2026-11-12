package com.fidecode.beebite.infrastructure.printing;

import static org.junit.jupiter.api.Assertions.*;

import com.fidecode.beebite.domain.printing.entity.Printer;
import com.fidecode.beebite.domain.printing.exception.PrinterConnectionException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TcpPrinterGatewayTest {

    private final TcpPrinterGateway gateway = new TcpPrinterGateway();
    private final UUID storeId = UUID.randomUUID();

    @Test
    void enderecoNuloOuVazioEhRejeitado() {
        var printer = new Printer(storeId, "Caixa", "NETWORK");
        printer.configureAddress(null);

        assertThrows(PrinterConnectionException.class, () -> gateway.send(printer, new byte[] { 0x1B, 0x40 }));
    }

    @Test
    void loopbackEhBloqueadoParaEvitarSsrf() {
        var printer = new Printer(storeId, "Caixa", "NETWORK");
        printer.configureAddress("127.0.0.1");

        assertThrows(PrinterConnectionException.class, () -> gateway.send(printer, new byte[] { 0x1B, 0x40 }));
    }

    @Test
    void linkLocalMetadataDeNuvemEhBloqueado() {
        var printer = new Printer(storeId, "Caixa", "NETWORK");
        printer.configureAddress("169.254.169.254");

        assertThrows(PrinterConnectionException.class, () -> gateway.send(printer, new byte[] { 0x1B, 0x40 }));
    }

    @Test
    void portaRestritaEhRejeitada() {
        var printer = new Printer(storeId, "Caixa", "NETWORK");
        printer.configureAddress("192.168.1.100");
        // SSH port 22
        printer.configureHardware(80, 22, "CP860", true);

        assertThrows(PrinterConnectionException.class, () -> gateway.send(printer, new byte[] { 0x1B, 0x40 }));
    }

    @Test
    void falhaDeConexaoViraPrinterConnectionException() {
        var printer = new Printer(storeId, "Caixa", "NETWORK");
        // IP não roteável com timeout rápido
        printer.configureAddress("192.0.2.1");
        printer.configureHardware(80, 9100, "CP860", true);

        assertThrows(PrinterConnectionException.class, () -> gateway.send(printer, new byte[] { 0x1B, 0x40 }));
    }
}
