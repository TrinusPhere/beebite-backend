package com.fidecode.biteflow.application.printing;

import com.fidecode.biteflow.domain.printing.exception.PrinterNotFoundException;
import com.fidecode.biteflow.domain.printing.repository.PrinterRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class TestPrinterUseCase {
    private final PrinterRepository repo;

    @Inject
    public TestPrinterUseCase(PrinterRepository repo) {
        this.repo = repo;
    }

    public Map<String, Object> execute(UUID storeId, UUID id) {
        var p = repo.findById(id).orElseThrow(() -> new PrinterNotFoundException(id.toString()));
        StoreGuard.enforce(p, storeId);
        return Map.of("success", true, "simulated", true, "message",
                "Configuração validada. O envio físico deve ser executado por um agente de impressão.");
    }
}
