package com.fidecode.biteflow.application.printing;

import com.fidecode.biteflow.domain.printing.exception.PrinterNotFoundException;
import com.fidecode.biteflow.domain.printing.repository.PrinterRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class DeletePrinterUseCase {
    private final PrinterRepository repo;

    @Inject
    public DeletePrinterUseCase(PrinterRepository repo) {
        this.repo = repo;
    }

    public void execute(UUID storeId, UUID id) {
        var p = repo.findById(id).orElseThrow(() -> new PrinterNotFoundException(id.toString()));
        StoreGuard.enforce(p, storeId);
        repo.deleteById(id);
    }
}
