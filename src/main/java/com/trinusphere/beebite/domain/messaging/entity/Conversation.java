package com.trinusphere.beebite.domain.messaging.entity;

import com.trinusphere.beebite.domain.shared.StoreGuard;
import java.time.LocalDateTime;
import java.util.*;

public class Conversation implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private UUID orderId;
    private final String channel;
    private String operation;
    private String customerName;
    private String customerPhone;
    private String status;
    private String externalId;
    private int unread;
    private List<ConversationMessage> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Conversation(UUID storeId, String channel) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (channel == null || channel.isBlank())
            throw new IllegalArgumentException("channel é obrigatório.");
        this.storeId = storeId;
        this.channel = channel.toUpperCase();
        this.operation = "DELIVERY";
        this.status = "IN_PROGRESS";
        this.messages = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Conversation reconstitute(UUID id, UUID storeId, UUID orderId, String channel, String operation,
            String customerName, String customerPhone, String status, String externalId, int unread,
            List<ConversationMessage> messages, LocalDateTime createdAt, LocalDateTime updatedAt) {
        var c = new Conversation(storeId, channel);
        c.id = id;
        c.orderId = orderId;
        c.operation = operation;
        c.customerName = customerName;
        c.customerPhone = customerPhone;
        c.status = status;
        c.externalId = externalId;
        c.unread = Math.max(0, unread);
        c.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        c.createdAt = createdAt;
        c.updatedAt = updatedAt;
        return c;
    }

    public void associateOrder(UUID orderId, String operation, String customerName, String customerPhone) {
        this.orderId = orderId;
        if (operation != null)
            this.operation = operation;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        touch();
    }

    public void updateStatus(String status) {
        if (status != null)
            this.status = status;
        touch();
    }

    public void updateUnread(int unread) {
        this.unread = Math.max(0, unread);
        touch();
    }

    public void waitingCustomer() {
        this.status = "WAITING_CUSTOMER";
        this.unread = 0;
        touch();
    }

    public void addMessage(ConversationMessage message) {
        if (message != null) {
            this.messages.add(message);
            touch();
        }
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getChannel() {
        return channel;
    }

    public String getOperation() {
        return operation;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getStatus() {
        return status;
    }

    public String getExternalId() {
        return externalId;
    }

    public int getUnread() {
        return unread;
    }

    public List<ConversationMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
