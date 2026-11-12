package com.fidecode.beebite.application.financial;

import com.fidecode.beebite.application.financial.dto.SalesSummaryResponse;
import com.fidecode.beebite.domain.financial.entity.FinancialRecord;
import com.fidecode.beebite.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.beebite.domain.financial.valueobject.FinancialStatus;
import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSalesSummaryUseCaseTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @InjectMocks
    private GetSalesSummaryUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveRetornarResumoDeVendasDoPeriodo() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        var record1 = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(),
                LocalDate.of(2026, 8, 15), java.time.LocalTime.of(10, 0), new BigDecimal("50.00"), PaymentMethod.PIX,
                FinancialStatus.CONCLUIDO, List.of(), java.time.LocalDateTime.now());

        var record2 = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(),
                LocalDate.of(2026, 8, 16), java.time.LocalTime.of(11, 0), new BigDecimal("30.00"),
                PaymentMethod.DINHEIRO, FinancialStatus.CONCLUIDO, List.of(), java.time.LocalDateTime.now());

        // Período atual
        when(financialRecordRepository.findByStoreIdAndDateRange(STORE_ID, startDate, endDate))
                .thenReturn(List.of(record1, record2));
        // Período anterior (mesmo tamanho, antes)
        when(financialRecordRepository.findByStoreIdAndDateRange(eq(STORE_ID), any(LocalDate.class),
                eq(startDate.minusDays(1)))).thenReturn(Collections.emptyList());

        // Act
        SalesSummaryResponse response = useCase.execute(STORE_ID, startEpoch, endEpoch);

        // Assert
        assertEquals(new BigDecimal("80.00"), response.revenue());
        assertEquals(2, response.totalOrders());
        assertEquals(startEpoch, response.startDate());
        assertEquals(endEpoch, response.endDate());
    }

    @Test
    void deveRetornarZeroQuandoSemRegistros() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        when(financialRecordRepository.findByStoreIdAndDateRange(eq(STORE_ID), any(), any()))
                .thenReturn(Collections.emptyList());

        SalesSummaryResponse response = useCase.execute(STORE_ID, startEpoch, endEpoch);

        assertEquals(BigDecimal.ZERO, response.revenue());
        assertEquals(0, response.totalOrders());
    }

    @Test
    void deveExcluirRegistrosCanceladosDaSoma() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        var recordOk = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(),
                LocalDate.of(2026, 8, 15), java.time.LocalTime.of(10, 0), new BigDecimal("100.00"), PaymentMethod.PIX,
                FinancialStatus.CONCLUIDO, List.of(), java.time.LocalDateTime.now());

        var recordCanceled = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(),
                LocalDate.of(2026, 8, 16), java.time.LocalTime.of(11, 0), new BigDecimal("50.00"),
                PaymentMethod.DINHEIRO, FinancialStatus.CANCELADO, List.of(), java.time.LocalDateTime.now());

        when(financialRecordRepository.findByStoreIdAndDateRange(STORE_ID, startDate, endDate))
                .thenReturn(List.of(recordOk, recordCanceled));
        when(financialRecordRepository.findByStoreIdAndDateRange(eq(STORE_ID), any(LocalDate.class),
                eq(startDate.minusDays(1)))).thenReturn(Collections.emptyList());

        SalesSummaryResponse response = useCase.execute(STORE_ID, startEpoch, endEpoch);

        assertEquals(new BigDecimal("100.00"), response.revenue());
        assertEquals(1, response.totalOrders());
    }
}
