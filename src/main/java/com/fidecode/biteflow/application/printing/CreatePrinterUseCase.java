package com.fidecode.biteflow.application.printing;

import com.fidecode.biteflow.application.printing.dto.*;
import com.fidecode.biteflow.domain.printing.entity.Printer;
import com.fidecode.biteflow.domain.printing.repository.PrinterRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class CreatePrinterUseCase {
    private final PrinterRepository repo;

    @Inject
    public CreatePrinterUseCase(PrinterRepository repo) {
        this.repo = repo;
    }

    public PrinterResponse execute(UUID storeId, PrinterRequest request) {
        var p = new Printer(storeId, request.name(), request.connectionType());
        p.configureAddress(request.address());
        if (request.active() != null)
            p.changeActive(request.active());
        return PrintingDtoMapper.toResponse(repo.save(p));
    }
}
