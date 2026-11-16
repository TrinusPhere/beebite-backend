package com.trinusphere.beebite.domain.messaging.repository;

import com.trinusphere.beebite.domain.messaging.entity.*;
import java.util.*;

public interface ConversationRepository {
    List<Conversation> findAllByStoreId(UUID storeId);

    Optional<Conversation> findById(UUID id);

    Optional<Conversation> findByStoreOrderChannel(UUID storeId, UUID orderId, String channel);

    Conversation save(Conversation c);

    ConversationMessage saveMessage(ConversationMessage m);
}
