package com.trinusphere.beebite.application.printing.port;

import com.trinusphere.beebite.domain.printing.entity.Printer;

/**
 * Port: entrega bytes a uma impressora acessível pelo backend (rede).
 *
 * @throws com.trinusphere.beebite.domain.printing.exception.PrinterConnectionException
 *             quando a entrega falha
 */
public interface PrinterGateway {
    void send(Printer printer, byte[] data);
}

