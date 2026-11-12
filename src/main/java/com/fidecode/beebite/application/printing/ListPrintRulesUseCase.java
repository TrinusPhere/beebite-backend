package com.fidecode.beebite.application.printing;

import com.fidecode.beebite.application.printing.dto.PrintRuleResponse;
import com.fidecode.beebite.domain.printing.repository.PrintRuleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class ListPrintRulesUseCase {
    private final PrintRuleRepository repo;

    @Inject
    public ListPrintRulesUseCase(PrintRuleRepository repo) {
        this.repo = repo;
    }

    public List<PrintRuleResponse> execute(UUID storeId) {
        return repo.findAllByStoreId(storeId).stream().map(PrintingDtoMapper::toResponse).toList();
    }
}
