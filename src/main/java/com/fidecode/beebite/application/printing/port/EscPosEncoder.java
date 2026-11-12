package com.fidecode.beebite.application.printing.port;

import com.fidecode.beebite.domain.printing.entity.Printer;
import com.fidecode.beebite.domain.printing.receipt.ReceiptDocument;

/** Port: converte um {@link ReceiptDocument} em bytes ESC/POS para a impressora informada. */
public interface EscPosEncoder {
    byte[] encode(ReceiptDocument document, Printer printer);
}

