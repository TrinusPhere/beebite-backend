package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.messaging.entity.*;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.*;

public final class ConversationMapper {
    private ConversationMapper() {
    }

    public static Conversation toDomain(ConversationEntity e) {
        return Conversation.reconstitute(e.id, e.storeId, e.orderId, e.channel, e.operation, e.customerName,
                e.customerPhone, e.status, e.externalId, e.unreadCount,
                e.messages.stream().map(ConversationMapper::toDomain).toList(), e.createdAt, e.updatedAt);
    }

    public static ConversationMessage toDomain(ConversationMessageEntity e) {
        return ConversationMessage.reconstitute(e.id, e.conversation.id, e.direction, e.content, e.deliveryStatus,
                e.externalId, e.createdAt, e.readAt);
    }
}
