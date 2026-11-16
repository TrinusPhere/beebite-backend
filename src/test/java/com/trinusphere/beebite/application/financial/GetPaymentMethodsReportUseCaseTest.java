package com.trinusphere.beebite.application.financial;

import com.trinusphere.beebite.application.financial.dto.PaymentMethodReportResponse;
import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.repository.FinancialRecordRepository;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialStatus;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
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
class GetPaymentMethodsReportUseCaseTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @InjectMocks
    private GetPaymentMethodsReportUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveRetornarDistribuicaoPorFormaDePagamento() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        var record1 = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(), startDate,
                java.time.LocalTime.of(10, 0), new BigDecimal("50.00"), PaymentMethod.PIX, FinancialStatus.CONCLUIDO,
                List.of(), java.time.LocalDateTime.now());

        var record2 = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(), startDate,
                java.time.LocalTime.of(11, 0), new BigDecimal("30.00"), PaymentMethod.PIX, FinancialStatus.CONCLUIDO,
                List.of(), java.time.LocalDateTime.now());

        var record3 = FinancialRecord.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(), startDate,
                java.time.LocalTime.of(12, 0), new BigDecimal("40.00"), PaymentMethod.CREDITO,
                FinancialStatus.CONCLUIDO, List.of(), java.time.LocalDateTime.now());

        when(financialRecordRepository.findByStoreIdAndDateRange(STORE_ID, startDate, endDate))
                .thenReturn(List.of(record1, record2, record3));

        PaymentMethodReportResponse response = useCase.execute(STORE_ID, startEpoch, endEpoch);

        assertEquals(2, response.methods().size());
        // PIX deve vir primeiro (maior valor total: 80)
        assertEquals("PIX", response.methods().get(0).method());
        assertEquals(2, response.methods().get(0).quantity());
        assertEquals(new BigDecimal("80.00"), response.methods().get(0).totalValue());
    }
}
