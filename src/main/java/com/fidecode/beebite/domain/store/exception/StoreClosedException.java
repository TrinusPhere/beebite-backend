package com.fidecode.beebite.domain.store.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class StoreClosedException extends BusinessRuleException {
    public StoreClosedException() {
        super("A loja está fechada para pedidos online.");
    }
}
