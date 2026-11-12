package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.domain.messaging.entity.*;
import com.fidecode.beebite.domain.messaging.repository.ConversationRepository;
import com.fidecode.beebite.domain.messaging.exception.ConversationNotFoundException;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.*;
import com.fidecode.beebite.infrastructure.persistence.panache.mapper.ConversationMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class PanacheConversationRepository implements ConversationRepository {
    @Inject
    ConversationPanacheRepo repo;
    @Inject
    ConversationMessagePanacheRepo msgRepo;

    public List<Conversation> findAllByStoreId(UUID s) {
        return repo.find("storeId = ?1 order by updatedAt desc", s).list().stream().map(ConversationMapper::toDomain)
                .toList();
    }

    public Optional<Conversation> findById(UUID id) {
        return repo.findByIdOptional(id).map(ConversationMapper::toDomain);
    }

    public Optional<Conversation> findByStoreOrderChannel(UUID s, UUID o, String c) {
        return repo.find("storeId = ?1 and orderId = ?2 and channel = ?3", s, o, c.toUpperCase()).firstResultOptional()
                .map(ConversationMapper::toDomain);
    }

    @Transactional
    public Conversation save(Conversation c) {
        ConversationEntity e = c.getId() != null ? repo.findById(c.getId()) : null;
        if (e == null) {
            e = new ConversationEntity();
            e.storeId = c.getStoreId();
            e.channel = c.getChannel();
            e.createdAt = LocalDateTime.now();
        }
        e.orderId = c.getOrderId();
        e.operation = c.getOperation();
        e.customerName = c.getCustomerName();
        e.customerPhone = c.getCustomerPhone();
        e.status = c.getStatus();
        e.externalId = c.getExternalId();
        e.unreadCount = c.getUnread();
        e.updatedAt = LocalDateTime.now();
        if (e.id == null)
            repo.persist(e);
        return ConversationMapper.toDomain(e);
    }

    @Transactional
    public ConversationMessage saveMessage(ConversationMessage m) {
        var c = repo.findById(m.getConversationId());
        if (c == null)
            throw new ConversationNotFoundException(m.getConversationId().toString());
        var e = new ConversationMessageEntity();
        e.conversation = c;
        e.direction = m.getDirection();
        e.content = m.getContent();
        e.deliveryStatus = m.getDeliveryStatus();
        e.externalId = m.getExternalId();
        e.createdAt = LocalDateTime.now();
        msgRepo.persist(e);
        c.updatedAt = LocalDateTime.now();
        return ConversationMapper.toDomain(e);
    }
}
