package com.fidecode.beebite.infrastructure.printing;

import com.fidecode.beebite.application.printing.port.PrinterGateway;
import com.fidecode.beebite.domain.printing.entity.Printer;
import com.fidecode.beebite.domain.printing.exception.PrinterConnectionException;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

@ApplicationScoped
public class TcpPrinterGateway implements PrinterGateway {

    private static final int TIMEOUT_MS = 3000;
    private static final int MIN_PORT = 1024;
    private static final int MAX_PORT = 65535;

    @Override
    public void send(Printer printer, byte[] data) {
        String address = printer.getAddress();
        int port = printer.getPort();

        validateDestination(printer.getName(), address, port);

        try (Socket socket = new Socket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            socket.connect(new InetSocketAddress(address, port), TIMEOUT_MS);

            OutputStream out = socket.getOutputStream();
            out.write(data);
            out.flush();
        } catch (Exception e) {
            throw new PrinterConnectionException(
                    "Falha ao comunicar com a impressora '" + printer.getName() + "' em " + address + ":" + port
                            + ": " + e.getMessage()
            );
        }
    }

    private void validateDestination(String printerName, String address, int port) {
        if (address == null || address.isBlank()) {
            throw new PrinterConnectionException(
                    "Endereço IP da impressora '" + printerName + "' não configurado."
            );
        }

        if (port < MIN_PORT || port > MAX_PORT) {
            throw new PrinterConnectionException(
                    "Porta " + port + " da impressora '" + printerName + "' não é permitida (use 1024-65535)."
            );
        }

        try {
            InetAddress inet = InetAddress.getByName(address.trim());
            if (inet.isLoopbackAddress()) {
                throw new PrinterConnectionException(
                        "Endereço loopback (127.x.x.x) não é permitido para envio de impressão por segurança."
                );
            }
            if (inet.isLinkLocalAddress()) {
                throw new PrinterConnectionException(
                        "Endereço link-local (169.254.x.x) não é permitido por segurança."
                );
            }
            if (inet.isMulticastAddress() || inet.isAnyLocalAddress()) {
                throw new PrinterConnectionException(
                        "Endereço inválido para impressora de rede: " + address
                );
            }
        } catch (UnknownHostException e) {
            throw new PrinterConnectionException(
                    "Não foi possível resolver o endereço da impressora '" + printerName + "': " + address
            );
        }
    }
}
