package com.fidecode.biteflow.infrastructure.event;

import com.fidecode.biteflow.application.financial.CreateFinancialRecordUseCase;
import com.fidecode.biteflow.application.inventory.DeductOrderInventoryUseCase;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.financial.event.OrderFinalizedEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

/**
 * Observador CDI — reage ao evento de pedido finalizado. Responsabilidades: 1. Criar FinancialRecord 2. Incrementar
 * totalDeliveries do driver (se presente) 3. Incrementar totalOrders do customer (se presente)
 */
@ApplicationScoped
public class OrderFinalizedObserver {

    private static final Logger LOG = Logger.getLogger(OrderFinalizedObserver.class);

    private final CreateFinancialRecordUseCase createFinancialRecordUseCase;
    private final DeliveryDriverRepository deliveryDriverRepository;
    private final CustomerRepository customerRepository;
    private final DeductOrderInventoryUseCase deductOrderInventoryUseCase;

    @Inject
    public OrderFinalizedObserver(CreateFinancialRecordUseCase createFinancialRecordUseCase,
            DeliveryDriverRepository deliveryDriverRepository, CustomerRepository customerRepository,
            DeductOrderInventoryUseCase deductOrderInventoryUseCase) {
        this.createFinancialRecordUseCase = createFinancialRecordUseCase;
        this.deliveryDriverRepository = deliveryDriverRepository;
        this.customerRepository = customerRepository;
        this.deductOrderInventoryUseCase = deductOrderInventoryUseCase;
    }

    @Transactional
    public void onOrderFinalized(@Observes OrderFinalizedEvent event) {
        LOG.infof("OrderFinalizedEvent recebido — orderId=%s, storeId=%s", event.getOrderId(), event.getStoreId());

        // 1. Baixar estoque usando a mesma fonte de verdade do pedido
        deductOrderInventoryUseCase.execute(event);

        // 2. Criar registro financeiro
        createFinancialRecordUseCase.execute(event);

        // 3. Incrementar totalDeliveries do driver
        if (event.getDriverId() != null) {
            deliveryDriverRepository.findById(event.getDriverId()).ifPresent(driver -> {
                driver.incrementDeliveries();
                deliveryDriverRepository.save(driver);
                LOG.infof("Driver %s totalDeliveries incrementado", driver.getName());
            });
        }

        // 4. Incrementar totalOrders do customer
        if (event.getCustomerId() != null) {
            customerRepository.findById(event.getCustomerId()).ifPresent(customer -> {
                customer.incrementTotalOrders();
                customerRepository.save(customer);
                LOG.infof("Customer %s totalOrders incrementado", customer.getName());
            });
        }
    }
}
