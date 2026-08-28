package com.fidecode.biteflow.domain.order.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class BatchOrderNotFoundException extends BusinessRuleException {
    public BatchOrderNotFoundException() {
        super("Alguns pedidos não foram encontrados para a operação em lote.");
    }
}
