package com.trinusphere.beebite.domain.order.entity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Modificador aplicado a um item do pedido — snapshot de dados no momento da criação.
 */
public class OrderItemModifier {

    private UUID id;
    private UUID groupId;
    private String groupName;
    private UUID optionId;
    private String optionName;
    private BigDecimal price;

    public OrderItemModifier(UUID groupId, String groupName, UUID optionId, String optionName, BigDecimal price) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    // ── Getters ──
    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public UUID getOptionId() {
        return optionId;
    }

    public String getOptionName() {
        return optionName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    // ── Setters ──
    public void setId(UUID id) {
        this.id = id;
    }
}
