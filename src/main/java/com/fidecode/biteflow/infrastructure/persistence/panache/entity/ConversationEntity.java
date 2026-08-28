package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "conversation")
@SQLRestriction("deleted_at IS NULL")
public class ConversationEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @Column(name = "store_id")
    public UUID storeId;
    @Column(name = "order_id")
    public UUID orderId;
    public String channel;
    public String operation;
    @Column(name = "customer_name")
    public String customerName;
    @Column(name = "customer_phone")
    public String customerPhone;
    public String status;
    @Column(name = "external_id")
    public String externalId;
    @Column(name = "unread_count")
    public int unreadCount;
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    public List<ConversationMessageEntity> messages = new ArrayList<>();
}
