package com.fidecode.biteflow.application.shared.port;

import java.util.function.Supplier;

/** Port de aplicação para delimitar unidade transacional sem acoplar Use Cases a JTA/JPA. */
public interface TransactionExecutor {
    <T> T execute(Supplier<T> action);

    default void execute(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }
}
