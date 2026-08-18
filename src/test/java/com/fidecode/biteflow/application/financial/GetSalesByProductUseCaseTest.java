package com.fidecode.biteflow.application.financial;

import com.fidecode.biteflow.application.financial.dto.SalesByProductResponse;
import com.fidecode.biteflow.domain.financial.entity.FinancialRecord;
import com.fidecode.biteflow.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.biteflow.domain.financial.valueobject.FinancialRecordProduct;
import com.fidecode.biteflow.domain.financial.valueobject.FinancialStatus;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSalesByProductUseCaseTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @InjectMocks
    private GetSalesByProductUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveRetornarRankingDeVendasPorProduto() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        var xBacon = new FinancialRecordProduct(UUID.randomUUID(), "X-Bacon", 2, new BigDecimal("35.00"));
        var batata = new FinancialRecordProduct(UUID.randomUUID(), "Batata Frita", 3, new BigDecimal("12.00"));
        var xBacon2 = new FinancialRecordProduct(UUID.randomUUID(), "X-Bacon", 1, new BigDecimal("35.00"));

        var record1 = FinancialRecord.reconstitute(
            UUID.randomUUID(), STORE_ID, UUID.randomUUID(),
            startDate, java.time.LocalTime.of(10, 0),
            new BigDecimal("106.00"), PaymentMethod.PIX,
            FinancialStatus.CONCLUIDO, List.of(xBacon, batata), java.time.LocalDateTime.now());

        var record2 = FinancialRecord.reconstitute(
            UUID.randomUUID(), STORE_ID, UUID.randomUUID(),
            startDate.plusDays(1), java.time.LocalTime.of(11, 0),
            new BigDecimal("35.00"), PaymentMethod.CREDITO,
            FinancialStatus.CONCLUIDO, List.of(xBacon2), java.time.LocalDateTime.now());

        when(financialRecordRepository.findByStoreIdAndDateRange(STORE_ID, startDate, endDate))
            .thenReturn(List.of(record1, record2));

        SalesByProductResponse response = useCase.execute(STORE_ID, startEpoch, endEpoch);

        assertEquals(2, response.products().size());
        // X-Bacon deve vir primeiro (3 unidades, R$105)
        assertEquals("X-Bacon", response.products().get(0).productName());
        assertEquals(3, response.products().get(0).quantity());
        assertEquals(new BigDecimal("105.00"), response.products().get(0).totalValue());
        // Batata em segundo (3 unidades, R$36)
        assertEquals("Batata Frita", response.products().get(1).productName());
    }

    @Test
    void deveExcluirRegistrosCancelados() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        var product = new FinancialRecordProduct(UUID.randomUUID(), "Suco", 1, new BigDecimal("8.00"));
        var canceledRecord = FinancialRecord.reconstitute(
            UUID.randomUUID(), STORE_ID, UUID.randomUUID(),
            startDate, java.time.LocalTime.of(10, 0),
            new BigDecimal("8.00"), PaymentMethod.PIX,
            FinancialStatus.CANCELADO, List.of(product), java.time.LocalDateTime.now());

        when(financialRecordRepository.findByStoreIdAndDateRange(STORE_ID, startDate, endDate))
            .thenReturn(List.of(canceledRecord));

        SalesByProductResponse response = useCase.execute(STORE_ID, startEpoch, endEpoch);

        assertTrue(response.products().isEmpty());
    }
}
