package com.fidecode.beebite.application.financial;

import com.fidecode.beebite.application.financial.dto.FinancialRecordResponse;
import com.fidecode.beebite.domain.financial.entity.FinancialRecord;
import com.fidecode.beebite.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.beebite.domain.financial.valueobject.FinancialRecordProduct;
import com.fidecode.beebite.domain.financial.valueobject.FinancialStatus;
import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;
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
class ListFinancialRecordsUseCaseTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @InjectMocks
    private ListFinancialRecordsUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveListarRegistrosFinanceirosPorPeriodo() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        long startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        UUID recordId = UUID.randomUUID();
        var products = List.of(new FinancialRecordProduct(UUID.randomUUID(), "X-Bacon", 2, new BigDecimal("35.00")));
        var record = FinancialRecord.reconstitute(recordId, STORE_ID, UUID.randomUUID(), startDate,
                java.time.LocalTime.of(10, 0), new BigDecimal("70.00"), PaymentMethod.PIX, FinancialStatus.CONCLUIDO,
                products, java.time.LocalDateTime.now());

        when(financialRecordRepository.findByStoreIdAndDateRange(STORE_ID, startDate, endDate))
                .thenReturn(List.of(record));

        List<FinancialRecordResponse> response = useCase.execute(STORE_ID, startEpoch, endEpoch);

        assertEquals(1, response.size());
        assertEquals(recordId, response.get(0).id());
        assertEquals("PIX", response.get(0).paymentMethod());
        assertEquals(1, response.get(0).products().size());
    }
}
