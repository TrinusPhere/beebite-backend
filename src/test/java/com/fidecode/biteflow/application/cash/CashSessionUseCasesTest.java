package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.application.cash.dto.CloseCashSessionRequest;
import com.fidecode.biteflow.application.cash.dto.OpenCashSessionRequest;
import com.fidecode.biteflow.application.cash.dto.CashSessionResponse;
import com.fidecode.biteflow.domain.cash.entity.CashRegister;
import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.exception.ActiveSessionAlreadyExistsException;
import com.fidecode.biteflow.domain.cash.exception.CashSessionNotFoundException;
import com.fidecode.biteflow.domain.cash.repository.CashMovementRepository;
import com.fidecode.biteflow.domain.cash.repository.CashRegisterRepository;
import com.fidecode.biteflow.domain.cash.repository.CashSessionRepository;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashSession Use Cases")
class CashSessionUseCasesTest {

    @Mock
    private CashSessionRepository sessionRepository;

    @Mock
    private CashRegisterRepository registerRepository;

    @Mock
    private CashMovementRepository movementRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("OpenCashSessionUseCase deve abrir sessão quando usuário não possui outra aberta")
    void shouldOpenSession() {
        OpenCashSessionUseCase useCase = new OpenCashSessionUseCase(sessionRepository, registerRepository);
        UUID storeId = UUID.randomUUID();
        UUID registerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CashRegister register = new CashRegister(storeId, "Caixa Balcão");
        register.setId(registerId);

        when(registerRepository.findById(registerId)).thenReturn(Optional.of(register));
        when(sessionRepository.findOpenSessionByUserId(storeId, userId)).thenReturn(Optional.empty());
        when(sessionRepository.findOpenSessionByRegisterId(storeId, registerId)).thenReturn(Optional.empty());
        when(sessionRepository.save(any(CashSession.class))).thenAnswer(i -> {
            CashSession s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        OpenCashSessionRequest req = new OpenCashSessionRequest(registerId.toString(), new BigDecimal("100.00"));
        CashSessionResponse res = useCase.execute(storeId, userId, req);

        assertNotNull(res);
        assertEquals("OPEN", res.status());
        assertEquals(new BigDecimal("100.00"), res.initialAmount());
        verify(sessionRepository).save(any(CashSession.class));
    }

    @Test
    @DisplayName("OpenCashSessionUseCase deve barrar se usuário já tem sessão aberta (Regra do Usuário)")
    void shouldRejectIfUserAlreadyHasOpenSession() {
        OpenCashSessionUseCase useCase = new OpenCashSessionUseCase(sessionRepository, registerRepository);
        UUID storeId = UUID.randomUUID();
        UUID registerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CashSession existingSession = new CashSession(storeId, registerId, userId, new BigDecimal("50.00"));

        when(sessionRepository.findOpenSessionByUserId(storeId, userId)).thenReturn(Optional.of(existingSession));

        OpenCashSessionRequest req = new OpenCashSessionRequest(registerId.toString(), new BigDecimal("100.00"));

        assertThrows(ActiveSessionAlreadyExistsException.class, () -> useCase.execute(storeId, userId, req));
    }

    @Test
    @DisplayName("CloseCashSessionUseCase deve fechar sessão calculando valores")
    void shouldCloseSession() {
        CloseCashSessionUseCase useCase = new CloseCashSessionUseCase(
                sessionRepository, movementRepository, paymentRepository
        );
        UUID storeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CashSession session = new CashSession(storeId, UUID.randomUUID(), userId, new BigDecimal("100.00"));
        session.setId(sessionId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(movementRepository.findBySessionId(sessionId)).thenReturn(List.of());
        when(paymentRepository.findByCashSessionId(sessionId)).thenReturn(List.of());
        when(sessionRepository.save(any(CashSession.class))).thenAnswer(i -> i.getArgument(0));

        CloseCashSessionRequest req = new CloseCashSessionRequest(new BigDecimal("100.00"), "Tudo conferido");
        CashSessionResponse res = useCase.execute(storeId, sessionId, req);

        assertEquals("CLOSED", res.status());
        assertEquals(new BigDecimal("0.00"), res.differenceAmount());
    }
}
