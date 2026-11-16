package com.trinusphere.beebite.application.printing;

import com.trinusphere.beebite.application.printing.dto.*;
import com.trinusphere.beebite.domain.printing.entity.Printer;
import com.trinusphere.beebite.domain.printing.repository.PrinterRepository;
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
        p.configureHardware(request.paperWidthMm(), request.port(), request.codePage(), request.cutPaper());
        if (request.active() != null)
            p.changeActive(request.active());
        return PrintingDtoMapper.toResponse(repo.save(p));
    }
}
