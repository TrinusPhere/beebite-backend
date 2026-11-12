package com.fidecode.beebite.infrastructure.transaction;

import com.fidecode.beebite.application.shared.port.TransactionExecutor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.function.Supplier;

@ApplicationScoped
public class QuarkusTransactionExecutor implements TransactionExecutor {
    @Override
    @Transactional
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }
}
