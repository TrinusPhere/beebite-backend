package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversation_message")
public class ConversationMessageEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    public ConversationEntity conversation;
    public String direction;
    @Column(columnDefinition = "TEXT")
    public String content;
    @Column(name = "delivery_status")
    public String deliveryStatus;
    @Column(name = "external_id")
    public String externalId;
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    @Column(name = "read_at")
    public LocalDateTime readAt;
}
