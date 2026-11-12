package com.fidecode.beebite.application.messaging;

import com.fidecode.beebite.application.messaging.dto.ConversationResponse;
import com.fidecode.beebite.domain.messaging.entity.*;
import java.time.format.DateTimeFormatter;

public final class ConversationDtoMapper {
    private ConversationDtoMapper() {
    }

    public static ConversationResponse toResponse(Conversation c) {
        return new ConversationResponse(c.getId().toString(), c.getChannel(), c.getCustomerName(), c.getCustomerPhone(),
                c.getStatus(), c.getOperation(), c.getOrderId() != null ? c.getOrderId().toString() : null,
                c.getUnread(), c.getMessages().stream().map(ConversationDtoMapper::toResponse).toList());
    }

    public static ConversationResponse.MessageResponse toResponse(ConversationMessage m) {
        return new ConversationResponse.MessageResponse(m.getId().toString(), m.getDirection(), m.getContent(),
                m.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), m.getDeliveryStatus());
    }
}
