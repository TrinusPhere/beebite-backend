package com.fidecode.biteflow.application.financial;

import com.fidecode.biteflow.application.financial.dto.GeneralCmvRequest;
import com.fidecode.biteflow.application.financial.dto.GeneralCmvResponse;
import com.fidecode.biteflow.domain.financial.entity.FinancialRecord;
import com.fidecode.biteflow.domain.financial.repository.FinancialRecordRepository;
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
class CalculateGeneralCmvUseCaseTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @InjectMocks
    private CalculateGeneralCmvUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveCalcularCmvGeralCorretamente() {
        // CMV = initialStock + purchases - finalStock = 5000 + 3000 - 4200 = 3800
        // grossRevenue = sum(records.totalValue) = 35600
        // CMV% = (3800 / 35600) * 100 = 10.67%
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        var request = new GeneralCmvRequest(startEpoch, endEpoch, new BigDecimal("5000.00"), new BigDecimal("3000.00"),
                new BigDecimal("4200.00"));

        var record = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(), startDate,
                java.time.LocalTime.of(10, 0), new BigDecimal("35600.00"), PaymentMethod.PIX, FinancialStatus.CONCLUIDO,
                List.of(), java.time.LocalDateTime.now());

        when(financialRecordRepository.findByStoreIdAndDateRange(STORE_ID, startDate, endDate))
                .thenReturn(List.of(record));

        GeneralCmvResponse response = useCase.execute(STORE_ID, request);

        assertEquals(new BigDecimal("35600.00"), response.grossRevenue());
        assertEquals(new BigDecimal("3800.00"), response.cmvValue());
        assertEquals(new BigDecimal("10.67"), response.cmvPercentage());
        assertEquals("GREEN", response.alertLevel());
    }

    @Test
    void deveRetornarYellowQuandoCmvEntre30e35() {
        // CMV = 1000 + 500 - 200 = 1300
        // grossRevenue = 4000
        // CMV% = (1300/4000)*100 = 32.50%
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        var request = new GeneralCmvRequest(startEpoch, endEpoch, new BigDecimal("1000.00"), new BigDecimal("500.00"),
                new BigDecimal("200.00"));

        var record = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(), startDate,
                java.time.LocalTime.of(10, 0), new BigDecimal("4000.00"), PaymentMethod.CREDITO,
                FinancialStatus.CONCLUIDO, List.of(), java.time.LocalDateTime.now());

        when(financialRecordRepository.findByStoreIdAndDateRange(STORE_ID, startDate, endDate))
                .thenReturn(List.of(record));

        GeneralCmvResponse response = useCase.execute(STORE_ID, request);

        assertEquals("YELLOW", response.alertLevel());
    }
}
