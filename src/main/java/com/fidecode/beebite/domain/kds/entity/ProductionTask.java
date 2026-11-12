package com.fidecode.beebite.domain.kds.entity;

import com.fidecode.beebite.domain.kds.exception.InvalidTaskTransitionException;
import com.fidecode.beebite.domain.kds.valueobject.TaskStatus;
import com.fidecode.beebite.domain.shared.StoreGuard;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductionTask implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private final UUID orderId;
    private final UUID orderItemId;
    private UUID sectorId;
    private String displayOrderId;
    private String orderType;
    private String orderChannel;
    private String customerName;
    private String itemName;
    private int quantity;
    private String notes;
    private String modifiersSummary;
    private String comboName;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime deletedAt;

    public ProductionTask(UUID storeId, UUID orderId, UUID orderItemId, UUID sectorId,
                          String displayOrderId, String orderType, String orderChannel,
                          String customerName, String itemName, int quantity,
                          String notes, String modifiersSummary, String comboName) {
        if (storeId == null) throw new IllegalArgumentException("storeId não pode ser nulo");
        if (orderId == null) throw new IllegalArgumentException("orderId não pode ser nulo");
        if (orderItemId == null) throw new IllegalArgumentException("orderItemId não pode ser nulo");
        if (itemName == null || itemName.trim().isEmpty()) throw new IllegalArgumentException("itemName não pode ser vazio");
        if (quantity <= 0) throw new IllegalArgumentException("quantity deve ser maior que zero");

        this.storeId = storeId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.sectorId = sectorId;
        this.displayOrderId = displayOrderId;
        this.orderType = orderType;
        this.orderChannel = orderChannel;
        this.customerName = customerName;
        this.itemName = itemName.trim();
        this.quantity = quantity;
        this.notes = notes;
        this.modifiersSummary = modifiersSummary;
        this.comboName = comboName;
        this.status = TaskStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public static ProductionTask reconstitute(UUID id, UUID storeId, UUID orderId, UUID orderItemId, UUID sectorId,
                                             String displayOrderId, String orderType, String orderChannel,
                                             String customerName, String itemName, int quantity,
                                             String notes, String modifiersSummary, String comboName,
                                             TaskStatus status, LocalDateTime createdAt, LocalDateTime startedAt,
                                             LocalDateTime completedAt, LocalDateTime canceledAt, LocalDateTime deletedAt) {
        ProductionTask task = new ProductionTask(storeId, orderId, orderItemId, sectorId, displayOrderId,
                orderType, orderChannel, customerName, itemName, quantity, notes, modifiersSummary, comboName);
        task.id = id;
        task.status = status;
        task.createdAt = createdAt;
        task.startedAt = startedAt;
        task.completedAt = completedAt;
        task.canceledAt = canceledAt;
        task.deletedAt = deletedAt;
        return task;
    }

    public void startPreparation() {
        if (this.status == TaskStatus.CANCELED) {
            throw new InvalidTaskTransitionException("Não é possível iniciar preparo de uma tarefa cancelada.");
        }
        if (this.status == TaskStatus.READY) {
            throw new InvalidTaskTransitionException("Tarefa já está concluída.");
        }
        this.status = TaskStatus.IN_PREPARATION;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status == TaskStatus.CANCELED) {
            throw new InvalidTaskTransitionException("Não é possível concluir uma tarefa cancelada.");
        }
        this.status = TaskStatus.READY;
        this.completedAt = LocalDateTime.now();
    }

    public void reopen() {
        if (this.status == TaskStatus.CANCELED) {
            throw new InvalidTaskTransitionException("Não é possível reabrir uma tarefa cancelada.");
        }
        this.status = TaskStatus.PENDING;
        this.completedAt = null;
    }

    public void cancel() {
        this.status = TaskStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getOrderItemId() {
        return orderItemId;
    }

    public UUID getSectorId() {
        return sectorId;
    }

    public String getDisplayOrderId() {
        return displayOrderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getOrderChannel() {
        return orderChannel;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }

    public String getModifiersSummary() {
        return modifiersSummary;
    }

    public String getComboName() {
        return comboName;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}

