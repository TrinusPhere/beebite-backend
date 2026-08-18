package com.fidecode.biteflow.application.dashboard;

import com.fidecode.biteflow.application.dashboard.dto.DashboardResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.financial.entity.FinancialRecord;
import com.fidecode.biteflow.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.biteflow.domain.financial.valueobject.FinancialStatus;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.*;
import com.fidecode.biteflow.domain.shared.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDashboardUseCaseTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DeliveryDriverRepository deliveryDriverRepository;

    @InjectMocks
    private GetDashboardUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveRetornarDashboardComKPIs() {
        // Arrange — Financial records (vendas do dia)
        var record = FinancialRecord.reconstitute(
            UUID.randomUUID(), STORE_ID, UUID.randomUUID(),
            LocalDate.now(), java.time.LocalTime.of(10, 0),
            new BigDecimal("150.00"), PaymentMethod.PIX,
            FinancialStatus.CONCLUIDO, List.of(), LocalDateTime.now());

        when(financialRecordRepository.findByStoreIdAndDateRange(eq(STORE_ID), any(), any()))
            .thenReturn(List.of(record));

        // Arrange — Pedidos ativos
        var activeOrder = Order.reconstitute(
            UUID.randomUUID(), STORE_ID, "#1024", null,
            null, "Mateus Silva", null,
            OrderType.DELIVERY, OrderStatus.PREPARO, PaymentMethod.PIX,
            null, null, OrderPlatform.OWN, null,
            BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("65.00"),
            false, List.of(), LocalDateTime.now(), LocalDateTime.now(), null);

        when(orderRepository.findActiveKanbanOrdersByStoreId(eq(STORE_ID), any()))
            .thenReturn(List.of(activeOrder));

        // Arrange — Clientes novos (hoje vs ontem)
        when(customerRepository.countByStoreIdAndCreatedSince(eq(STORE_ID), any()))
            .thenReturn(3L);

        // Arrange — Entregadores
        var driver = new DeliveryDriver(STORE_ID, "Carlos Silva", "(11) 99999-0000");
        driver.setId(UUID.randomUUID());
        when(deliveryDriverRepository.findAllByStoreId(eq(STORE_ID), eq(0), eq(100)))
            .thenReturn(new PageResult<>(List.of(driver), 0, 100, 1, 1));

        // Act
        DashboardResponse response = useCase.execute(STORE_ID);

        // Assert
        assertNotNull(response);
        assertFalse(response.stats().isEmpty());
        assertNotNull(response.lastUpdatedAt());
        assertEquals(1, response.deliveryTeam().size());
        assertEquals("Carlos Silva", response.deliveryTeam().get(0).name());
    }
}
