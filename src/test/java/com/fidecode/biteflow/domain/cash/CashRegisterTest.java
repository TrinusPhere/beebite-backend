package com.fidecode.biteflow.domain.cash;

import com.fidecode.biteflow.domain.cash.entity.CashRegister;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashRegister — Regras de Domínio")
class CashRegisterTest {

    @Test
    @DisplayName("Deve criar caixa físico ativo com sucesso")
    void shouldCreateCashRegister() {
        UUID storeId = UUID.randomUUID();
        CashRegister register = new CashRegister(storeId, "Caixa 01 - Balcão");

        assertNotNull(register);
        assertEquals(storeId, register.getStoreId());
        assertEquals("Caixa 01 - Balcão", register.getName());
        assertTrue(register.isActive());
        assertNotNull(register.getCreatedAt());
        assertNull(register.getDeletedAt());
    }

    @Test
    @DisplayName("Deve validar nome não vazio")
    void shouldValidateName() {
        UUID storeId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> new CashRegister(storeId, ""));
        assertThrows(IllegalArgumentException.class, () -> new CashRegister(storeId, "   "));
        assertThrows(IllegalArgumentException.class, () -> new CashRegister(null, "Caixa"));
    }

    @Test
    @DisplayName("Deve renomear caixa e desativar/reativar")
    void shouldUpdateCashRegister() {
        UUID storeId = UUID.randomUUID();
        CashRegister register = new CashRegister(storeId, "Caixa Principal");

        register.rename("Caixa Rápido");
        assertEquals("Caixa Rápido", register.getName());

        register.deactivate();
        assertFalse(register.isActive());

        register.activate();
        assertTrue(register.isActive());
    }

    @Test
    @DisplayName("Deve reconstituir caixa a partir da persistência")
    void shouldReconstituteCashRegister() {
        UUID id = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CashRegister register = CashRegister.reconstitute(id, storeId, "Caixa PDV", true, now, now, null);

        assertEquals(id, register.getId());
        assertEquals("Caixa PDV", register.getName());
        assertTrue(register.isActive());
    }
}
