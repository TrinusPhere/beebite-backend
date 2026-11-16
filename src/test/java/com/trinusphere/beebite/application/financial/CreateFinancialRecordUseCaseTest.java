package com.trinusphere.beebite.application.financial;

import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.event.OrderFinalizedEvent;
import com.trinusphere.beebite.domain.financial.repository.FinancialRecordRepository;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialStatus;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFinancialRecordUseCaseTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @InjectMocks
    private CreateFinancialRecordUseCase useCase;

    @Test
    void deveCriarRegistroFinanceiroAPartirDoEvento() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        var products = List
                .of(new OrderFinalizedEvent.ProductSnapshot(UUID.randomUUID(), "X-Bacon", 2, new BigDecimal("35.00")));
        var event = new OrderFinalizedEvent(orderId, storeId, new BigDecimal("70.00"), PaymentMethod.PIX, null, null,
                products);

        when(financialRecordRepository.save(any(FinancialRecord.class))).thenAnswer(inv -> {
            FinancialRecord r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        // Act
        useCase.execute(event);

        // Assert
        ArgumentCaptor<FinancialRecord> captor = ArgumentCaptor.forClass(FinancialRecord.class);
        verify(financialRecordRepository).save(captor.capture());

        FinancialRecord saved = captor.getValue();
        assertEquals(storeId, saved.getStoreId());
        assertEquals(orderId, saved.getOrderId());
        assertEquals(new BigDecimal("70.00"), saved.getTotalValue());
        assertEquals(PaymentMethod.PIX, saved.getPaymentMethod());
        assertEquals(FinancialStatus.CONCLUIDO, saved.getStatus());
        assertEquals(1, saved.getProducts().size());
        assertEquals("X-Bacon", saved.getProducts().get(0).getProductName());
    }

    @Test
    void deveCriarRegistroSemProdutosQuandoListaVazia() {
        var event = new OrderFinalizedEvent(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN,
                PaymentMethod.DINHEIRO, null, null, List.of());

        when(financialRecordRepository.save(any(FinancialRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(event);

        ArgumentCaptor<FinancialRecord> captor = ArgumentCaptor.forClass(FinancialRecord.class);
        verify(financialRecordRepository).save(captor.capture());
        assertTrue(captor.getValue().getProducts().isEmpty());
    }
}
