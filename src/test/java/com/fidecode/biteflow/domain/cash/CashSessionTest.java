package com.fidecode.biteflow.domain.cash;

import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.exception.SessionAlreadyClosedException;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashSession — Regras de Domínio")
class CashSessionTest {

    @Test
    @DisplayName("Deve abrir sessão de caixa no status OPEN com fundo de troco")
    void shouldOpenCashSession() {
        UUID storeId = UUID.randomUUID();
        UUID registerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal initialAmount = new BigDecimal("150.00");

        CashSession session = new CashSession(storeId, registerId, userId, initialAmount);

        assertNotNull(session);
        assertEquals(storeId, session.getStoreId());
        assertEquals(registerId, session.getCashRegisterId());
        assertEquals(userId, session.getUserId());
        assertEquals(initialAmount, session.getInitialAmount());
        assertEquals(CashSessionStatus.OPEN, session.getStatus());
        assertNotNull(session.getOpenedAt());
        assertNull(session.getClosedAt());
        assertNull(session.getExpectedAmount());
        assertNull(session.getCountedAmount());
        assertNull(session.getDifferenceAmount());
    }

    @Test
    @DisplayName("Deve fechar sessão calculando diferença e observações")
    void shouldCloseCashSession() {
        UUID storeId = UUID.randomUUID();
        CashSession session = new CashSession(storeId, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"));

        BigDecimal expected = new BigDecimal("450.00");
        BigDecimal counted = new BigDecimal("445.00"); // Falta 5 reais
        session.close(expected, counted, "Faltou R$ 5,00 em moedas de troco");

        assertEquals(CashSessionStatus.CLOSED, session.getStatus());
        assertNotNull(session.getClosedAt());
        assertEquals(expected, session.getExpectedAmount());
        assertEquals(counted, session.getCountedAmount());
        assertEquals(new BigDecimal("-5.00"), session.getDifferenceAmount());
        assertEquals("Faltou R$ 5,00 em moedas de troco", session.getClosingNotes());
    }

    @Test
    @DisplayName("Não deve permitir fechar sessão que já está fechada")
    void shouldNotCloseAlreadyClosedSession() {
        UUID storeId = UUID.randomUUID();
        CashSession session = new CashSession(storeId, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50.00"));

        session.close(new BigDecimal("100.00"), new BigDecimal("100.00"), null);

        assertThrows(SessionAlreadyClosedException.class, () ->
                session.close(new BigDecimal("100.00"), new BigDecimal("100.00"), null)
        );
    }
}
