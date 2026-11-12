package com.fidecode.beebite.application.cash;

import com.fidecode.beebite.application.cash.dto.CashMovementRequest;
import com.fidecode.beebite.application.cash.dto.CashMovementResponse;
import com.fidecode.beebite.domain.cash.entity.CashMovement;
import com.fidecode.beebite.domain.cash.entity.CashSession;
import com.fidecode.beebite.domain.cash.exception.SessionAlreadyClosedException;
import com.fidecode.beebite.domain.cash.repository.CashMovementRepository;
import com.fidecode.beebite.domain.cash.repository.CashSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashMovement Use Cases")
class CashMovementUseCasesTest {

    @Mock
    private CashMovementRepository movementRepository;

    @Mock
    private CashSessionRepository sessionRepository;

    @Test
    @DisplayName("AddCashMovementUseCase deve registrar sangria em sessão aberta")
    void shouldAddSangria() {
        AddCashMovementUseCase useCase = new AddCashMovementUseCase(movementRepository, sessionRepository);
        UUID storeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CashSession session = new CashSession(storeId, UUID.randomUUID(), userId, new BigDecimal("100.00"));
        session.setId(sessionId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(movementRepository.save(any(CashMovement.class))).thenAnswer(i -> {
            CashMovement m = i.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        CashMovementRequest req = new CashMovementRequest("SANGRIA", new BigDecimal("50.00"), "Retirada");
        CashMovementResponse res = useCase.execute(storeId, sessionId, userId, req);

        assertNotNull(res);
        assertEquals("SANGRIA", res.type());
        assertEquals(new BigDecimal("50.00"), res.amount());
        verify(movementRepository).save(any(CashMovement.class));
    }

    @Test
    @DisplayName("AddCashMovementUseCase não deve permitir movimentar sessão fechada")
    void shouldRejectOnClosedSession() {
        AddCashMovementUseCase useCase = new AddCashMovementUseCase(movementRepository, sessionRepository);
        UUID storeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CashSession session = new CashSession(storeId, UUID.randomUUID(), userId, new BigDecimal("100.00"));
        session.setId(sessionId);
        session.close(new BigDecimal("100.00"), new BigDecimal("100.00"), "Fechado");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        CashMovementRequest req = new CashMovementRequest("SANGRIA", new BigDecimal("50.00"), "Retirada");

        assertThrows(SessionAlreadyClosedException.class, () -> useCase.execute(storeId, sessionId, userId, req));
    }
}
