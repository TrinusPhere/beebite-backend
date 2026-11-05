package com.fidecode.biteflow.application.printing.port;

import com.fidecode.biteflow.domain.printing.entity.Printer;

/**
 * Port: entrega bytes a uma impressora acessível pelo backend (rede).
 *
 * @throws com.fidecode.biteflow.domain.printing.exception.PrinterConnectionException
 *             quando a entrega falha
 */
public interface PrinterGateway {
    void send(Printer printer, byte[] data);
}

