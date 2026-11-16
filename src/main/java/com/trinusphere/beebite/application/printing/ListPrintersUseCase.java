package com.trinusphere.beebite.application.printing;

import com.trinusphere.beebite.application.printing.dto.PrinterResponse;
import com.trinusphere.beebite.domain.printing.repository.PrinterRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class ListPrintersUseCase {
    private final PrinterRepository repo;

    @Inject
    public ListPrintersUseCase(PrinterRepository repo) {
        this.repo = repo;
    }

    public List<PrinterResponse> execute(UUID storeId) {
        return repo.findAllByStoreId(storeId).stream().map(PrintingDtoMapper::toResponse).toList();
    }
}
