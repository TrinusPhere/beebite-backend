package com.fidecode.beebite.application.printing;

import com.fidecode.beebite.application.printing.dto.PrintResultResponse;
import com.fidecode.beebite.domain.printing.exception.PrinterNotFoundException;
import com.fidecode.beebite.domain.printing.repository.PrinterRepository;
import com.fidecode.beebite.domain.printing.service.ReceiptLayoutService;
import com.fidecode.beebite.domain.shared.StoreGuard;
import com.fidecode.beebite.domain.store.exception.StoreNotFoundException;
import com.fidecode.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/** Imprime uma página de teste real (ESC/POS) na impressora, ou devolve os bytes para impressoras USB. */
@ApplicationScoped
public class TestPrinterUseCase {
    private final PrinterRepository repo;
    private final StoreRepository stores;
    private final ReceiptLayoutService layout;
    private final DispatchReceiptService dispatch;

    @Inject
    public TestPrinterUseCase(PrinterRepository repo, StoreRepository stores, ReceiptLayoutService layout,
            DispatchReceiptService dispatch) {
        this.repo = repo;
        this.stores = stores;
        this.layout = layout;
        this.dispatch = dispatch;
    }

    public PrintResultResponse execute(UUID storeId, UUID id) {
        var p = StoreGuard.enforce(repo.findById(id).orElseThrow(() -> new PrinterNotFoundException(id.toString())),
                storeId);
        var store = stores.findById(storeId).orElseThrow(() -> new StoreNotFoundException(storeId));
        return dispatch.dispatch(p, layout.buildTestPage(p.getName(), store.getName(), p.getPaperWidth()));
    }
}
