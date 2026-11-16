package com.trinusphere.beebite.application.messaging.dto;

public record ResolveConversationRequest(String orderId, String channel, String operation, String customerName,
        String customerPhone) {
}
