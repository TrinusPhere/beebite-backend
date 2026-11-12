package com.fidecode.beebite.application.printing;

import com.fidecode.beebite.application.printing.dto.PrintResultResponse;
import com.fidecode.beebite.application.printing.port.EscPosEncoder;
import com.fidecode.beebite.application.printing.port.PrinterGateway;
import com.fidecode.beebite.domain.printing.entity.Printer;
import com.fidecode.beebite.domain.printing.exception.PrinterConnectionException;
import com.fidecode.beebite.domain.printing.receipt.ReceiptDocument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Base64;

/**
 * Entrega um documento à impressora: por rede (backend envia) ou USB (devolve os bytes para o navegador enviar).
 * Centraliza as regras comuns a "imprimir pedido" e "testar impressora".
 */
@ApplicationScoped
public class DispatchReceiptService {
    private final EscPosEncoder encoder;
    private final PrinterGateway gateway;

    @Inject
    public DispatchReceiptService(EscPosEncoder encoder, PrinterGateway gateway) {
        this.encoder = encoder;
        this.gateway = gateway;
    }

    public PrintResultResponse dispatch(Printer printer, ReceiptDocument document) {
        if (!printer.isActive())
            throw new PrinterConnectionException("A impressora '" + printer.getName() + "' está inativa.");
        if (!printer.isNetwork() && !printer.isBrowserDriven())
            throw new PrinterConnectionException("O tipo de conexão " + printer.getConnectionType()
                    + " não é suportado pelo servidor. Use NETWORK ou USB_BROWSER.");

        byte[] data = encoder.encode(document, printer);
        if (printer.isBrowserDriven())
            return PrintResultResponse.browser(Base64.getEncoder().encodeToString(data));

        gateway.send(printer, data);
        return PrintResultResponse.sent("Enviado para a impressora '" + printer.getName() + "'.");
    }
}

