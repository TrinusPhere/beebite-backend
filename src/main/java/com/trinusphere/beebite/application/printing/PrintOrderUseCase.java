package com.trinusphere.beebite.application.printing;

import com.trinusphere.beebite.application.printing.dto.PrintOrderRequest;
import com.trinusphere.beebite.application.printing.dto.PrintResultResponse;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.exception.OrderNotFoundException;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.printing.entity.PrintTemplate;
import com.trinusphere.beebite.domain.printing.entity.Printer;
import com.trinusphere.beebite.domain.printing.exception.PrinterNotFoundException;
import com.trinusphere.beebite.domain.printing.repository.PrintTemplateRepository;
import com.trinusphere.beebite.domain.printing.repository.PrinterRepository;
import com.trinusphere.beebite.domain.printing.service.ReceiptLayoutService;
import com.trinusphere.beebite.domain.printing.valueobject.DocumentType;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class PrintOrderUseCase {
    private final OrderRepository orders;
    private final StoreRepository stores;
    private final PrinterRepository printers;
    private final PrintTemplateRepository templates;
    private final ReceiptLayoutService layout;
    private final DispatchReceiptService dispatch;

    @Inject
    public PrintOrderUseCase(OrderRepository orders, StoreRepository stores, PrinterRepository printers,
            PrintTemplateRepository templates, ReceiptLayoutService layout, DispatchReceiptService dispatch) {
        this.orders = orders;
        this.stores = stores;
        this.printers = printers;
        this.templates = templates;
        this.layout = layout;
        this.dispatch = dispatch;
    }

    public PrintResultResponse execute(UUID storeId, UUID orderId, PrintOrderRequest request) {
        Order order = StoreGuard.enforce(
                orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString())), storeId);
        Printer printer = StoreGuard.enforce(printers.findById(request.printerId())
                .orElseThrow(() -> new PrinterNotFoundException(request.printerId().toString())), storeId);
        var store = stores.findById(storeId).orElseThrow(() -> new StoreNotFoundException(storeId));
        DocumentType type = DocumentType.fromValue(request.documentType());
        PrintTemplate template = templates.findByStoreIdAndDocumentType(storeId, type)
                .orElseGet(() -> PrintTemplate.defaultFor(storeId, type));

        return dispatch.dispatch(printer, layout.build(order, store, template, printer.getPaperWidth()));
    }
}

