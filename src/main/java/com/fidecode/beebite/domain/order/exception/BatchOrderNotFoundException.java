package com.fidecode.beebite.domain.order.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class BatchOrderNotFoundException extends BusinessRuleException {
    public BatchOrderNotFoundException() {
        super("Alguns pedidos não foram encontrados para a operação em lote.");
    }
}
