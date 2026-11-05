package com.fidecode.biteflow.application.printing.port;

import com.fidecode.biteflow.domain.printing.entity.Printer;
import com.fidecode.biteflow.domain.printing.receipt.ReceiptDocument;

/** Port: converte um {@link ReceiptDocument} em bytes ESC/POS para a impressora informada. */
public interface EscPosEncoder {
    byte[] encode(ReceiptDocument document, Printer printer);
}

