package com.fidecode.biteflow.domain.kds;

import com.fidecode.biteflow.domain.kds.entity.ProductionTask;
import com.fidecode.biteflow.domain.kds.exception.InvalidTaskTransitionException;
import com.fidecode.biteflow.domain.kds.valueobject.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductionTask — Regras de Domínio")
class ProductionTaskTest {

    @Test
    @DisplayName("Deve criar tarefa de produção no status PENDING")
    void shouldCreateProductionTaskPending() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        ProductionTask task = new ProductionTask(
                storeId, orderId, orderItemId, sectorId, "#101",
                "DELIVERY", "PDV", "Mateus Silva",
                "Smash Burger", 2, "Sem cebola", "Queijo extra (+R$ 3,00)", null
        );

        assertNotNull(task);
        assertEquals(storeId, task.getStoreId());
        assertEquals(orderId, task.getOrderId());
        assertEquals(orderItemId, task.getOrderItemId());
        assertEquals(sectorId, task.getSectorId());
        assertEquals("#101", task.getDisplayOrderId());
        assertEquals("Smash Burger", task.getItemName());
        assertEquals(2, task.getQuantity());
        assertEquals("Sem cebola", task.getNotes());
        assertEquals("Queijo extra (+R$ 3,00)", task.getModifiersSummary());
        assertNull(task.getComboName());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertNotNull(task.getCreatedAt());
        assertNull(task.getStartedAt());
        assertNull(task.getCompletedAt());
        assertNull(task.getCanceledAt());
    }

    @Test
    @DisplayName("Deve iniciar preparo registrando data/hora")
    void shouldStartPreparation() {
        UUID storeId = UUID.randomUUID();
        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "#102",
                "DELIVERY", "PDV", "Cliente A", "Batata Rústica", 1, null, null, "Combo Smash"
        );

        task.startPreparation();

        assertEquals(TaskStatus.IN_PREPARATION, task.getStatus());
        assertNotNull(task.getStartedAt());
    }

    @Test
    @DisplayName("Deve finalizar produção registrando data/hora e saindo da tela ativa")
    void shouldCompleteProduction() {
        UUID storeId = UUID.randomUUID();
        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "#103",
                "SALAO", "PDV", "Mesa 04", "Smash Burger", 1, null, null, null
        );

        task.startPreparation();
        task.complete();

        assertEquals(TaskStatus.READY, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    @DisplayName("Deve permitir finalizar direto de PENDING (atalho na tela touch)")
    void shouldAllowCompleteDirectlyFromPending() {
        UUID storeId = UUID.randomUUID();
        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "#104",
                "SALAO", "PDV", "Mesa 05", "Refrigerante Cola Lata", 1, null, null, null
        );

        task.complete();

        assertEquals(TaskStatus.READY, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    @DisplayName("Deve reabrir tarefa concluída voltando para PENDING")
    void shouldReopenCompletedTask() {
        UUID storeId = UUID.randomUUID();
        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "#105",
                "DELIVERY", "IFOOD", "Cliente iFood", "Pizza Brotinho", 1, null, null, null
        );

        task.complete();
        assertEquals(TaskStatus.READY, task.getStatus());

        task.reopen();
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertNull(task.getCompletedAt());
    }

    @Test
    @DisplayName("Deve cancelar tarefa registrando data/hora")
    void shouldCancelTask() {
        UUID storeId = UUID.randomUUID();
        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "#106",
                "DELIVERY", "PDV", "Cliente B", "Smash Duplo", 1, null, null, null
        );

        task.cancel();

        assertEquals(TaskStatus.CANCELED, task.getStatus());
        assertNotNull(task.getCanceledAt());
    }

    @Test
    @DisplayName("Não deve permitir transição de tarefa cancelada")
    void shouldNotAllowTransitionOnCanceledTask() {
        UUID storeId = UUID.randomUUID();
        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "#107",
                "DELIVERY", "PDV", "Cliente C", "Smash Bacon", 1, null, null, null
        );

        task.cancel();

        assertThrows(InvalidTaskTransitionException.class, task::startPreparation);
        assertThrows(InvalidTaskTransitionException.class, task::complete);
        assertThrows(InvalidTaskTransitionException.class, task::reopen);
    }
}

