package com.fidecode.biteflow.domain.store.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class StoreClosedException extends BusinessRuleException {
    public StoreClosedException() {
        super("A loja está fechada para pedidos online.");
    }
}
