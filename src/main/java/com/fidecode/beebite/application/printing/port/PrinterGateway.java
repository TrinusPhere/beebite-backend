package com.fidecode.beebite.application.printing.port;

import com.fidecode.beebite.domain.printing.entity.Printer;

/**
 * Port: entrega bytes a uma impressora acessível pelo backend (rede).
 *
 * @throws com.fidecode.beebite.domain.printing.exception.PrinterConnectionException
 *             quando a entrega falha
 */
public interface PrinterGateway {
    void send(Printer printer, byte[] data);
}

