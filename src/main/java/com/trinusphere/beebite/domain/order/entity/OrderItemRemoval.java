package com.trinusphere.beebite.domain.order.entity;

import java.util.UUID;

/**
 * Ingrediente removido de um item do pedido — snapshot.
 */
public class OrderItemRemoval {

    private UUID id;
    private UUID ingredientId;
    private String ingredientName;

    public OrderItemRemoval(UUID ingredientId, String ingredientName) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
    }

    // ── Getters ──
    public UUID getId() {
        return id;
    }

    public UUID getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    // ── Setters ──
    public void setId(UUID id) {
        this.id = id;
    }
}
