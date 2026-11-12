package com.fidecode.beebite.domain.cash;

import com.fidecode.beebite.domain.cash.entity.CashMovement;
import com.fidecode.beebite.domain.cash.exception.InvalidMovementException;
import com.fidecode.beebite.domain.cash.valueobject.MovementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashMovement — Regras de Domínio")
class CashMovementTest {

    @Test
    @DisplayName("Deve registrar sangria com justificativa obrigatória")
    void shouldCreateSangria() {
        UUID storeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200.00");

        CashMovement movement = new CashMovement(
                storeId, sessionId, userId, MovementType.SANGRIA, amount, "Retirada para cofre"
        );

        assertNotNull(movement);
        assertEquals(storeId, movement.getStoreId());
        assertEquals(MovementType.SANGRIA, movement.getType());
        assertEquals(amount, movement.getAmount());
        assertEquals("Retirada para cofre", movement.getReason());
        assertNotNull(movement.getCreatedAt());
    }

    @Test
    @DisplayName("Deve registrar ajuste compensatório com justificativa")
    void shouldCreateAjusteCompensatorio() {
        UUID storeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CashMovement movement = new CashMovement(
                storeId, sessionId, userId, MovementType.AJUSTE_COMPENSATORIO,
                new BigDecimal("15.00"), "Ajuste de lançamento duplicado no cartão"
        );

        assertEquals(MovementType.AJUSTE_COMPENSATORIO, movement.getType());
        assertEquals(new BigDecimal("15.00"), movement.getAmount());
    }

    @Test
    @DisplayName("Deve rejeitar valor menor ou igual a zero e motivo vazio")
    void shouldRejectInvalidMovement() {
        UUID storeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThrows(InvalidMovementException.class, () ->
                new CashMovement(storeId, sessionId, userId, MovementType.SUPRIMENTO, BigDecimal.ZERO, "Troco extra")
        );
        assertThrows(InvalidMovementException.class, () ->
                new CashMovement(storeId, sessionId, userId, MovementType.SUPRIMENTO, new BigDecimal("-10.00"), "Troco extra")
        );
        assertThrows(InvalidMovementException.class, () ->
                new CashMovement(storeId, sessionId, userId, MovementType.SUPRIMENTO, new BigDecimal("10.00"), "   ")
        );
    }
}
