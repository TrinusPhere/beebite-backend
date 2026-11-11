package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import com.fidecode.biteflow.infrastructure.persistence.panache.converter.StringListConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_store_access", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_store_access", columnNames = { "user_id", "store_id" })
})
public class UserStoreAccessEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "role", nullable = false, length = 20)
    public String role;

    @Convert(converter = StringListConverter.class)
    @Column(name = "permissions", columnDefinition = "text[]", nullable = false)
    public List<String> permissions;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;
}
