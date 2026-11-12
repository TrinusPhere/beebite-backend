package com.fidecode.beebite.application.financial;

import com.fidecode.beebite.domain.financial.entity.FinancialRecord;
import com.fidecode.beebite.domain.financial.event.OrderFinalizedEvent;
import com.fidecode.beebite.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.beebite.domain.financial.valueobject.FinancialRecordProduct;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Use Case — Cria um registro financeiro a partir de um evento de pedido finalizado.
 */
@ApplicationScoped
public class CreateFinancialRecordUseCase {

    private final FinancialRecordRepository financialRecordRepository;

    @Inject
    public CreateFinancialRecordUseCase(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    public void execute(OrderFinalizedEvent event) {
        var products = event.getProducts().stream().map(snapshot -> new FinancialRecordProduct(snapshot.productId(),
                snapshot.productName(), snapshot.quantity(), snapshot.price())).toList();

        var record = new FinancialRecord(event.getStoreId(), event.getOrderId(), event.getTotalValue(),
                event.getPaymentMethod(), products);

        financialRecordRepository.save(record);
    }
}
