package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import java.time.LocalDateTime;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class UserEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "login", nullable = false, unique = true)
    public String login;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(name = "role", nullable = false)
    public String role;

    @Convert(converter = com.fidecode.biteflow.infrastructure.persistence.panache.converter.StringListConverter.class)
    @Column(name = "permissions", columnDefinition = "text[]")
    public List<String> permissions;

    @Column(name = "active", nullable = false)
    public boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @jakarta.persistence.Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}
