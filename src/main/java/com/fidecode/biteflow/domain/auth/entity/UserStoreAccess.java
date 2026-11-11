package com.fidecode.biteflow.domain.auth.entity;

import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UserStoreAccess implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID userId;
    private final UUID storeId;
    private Role role;
    private List<Permission> permissions;
    private final Instant createdAt;

    public UserStoreAccess(UUID userId, UUID storeId, Role role, List<Permission> permissions) {
        validate(userId, storeId, role);
        this.userId = userId;
        this.storeId = storeId;
        this.role = role;
        this.permissions = permissions != null ? List.copyOf(permissions) : List.of();
        this.createdAt = Instant.now();
    }

    private UserStoreAccess(UUID id, UUID userId, UUID storeId, Role role, List<Permission> permissions,
            Instant createdAt) {
        validate(userId, storeId, role);
        this.id = id;
        this.userId = userId;
        this.storeId = storeId;
        this.role = role;
        this.permissions = permissions != null ? List.copyOf(permissions) : List.of();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static UserStoreAccess reconstitute(UUID id, UUID userId, UUID storeId, Role role,
            List<Permission> permissions, Instant createdAt) {
        return new UserStoreAccess(id, userId, storeId, role, permissions, createdAt);
    }

    private static void validate(UUID userId, UUID storeId, Role role) {
        if (userId == null) {
            throw new IllegalArgumentException("userId é obrigatório.");
        }
        if (storeId == null) {
            throw new IllegalArgumentException("storeId é obrigatório.");
        }
        if (role == null) {
            throw new IllegalArgumentException("role é obrigatório.");
        }
    }

    public void updateAccess(Role role, List<Permission> permissions) {
        if (role == null) {
            throw new IllegalArgumentException("role é obrigatório.");
        }
        this.role = role;
        this.permissions = permissions != null ? List.copyOf(permissions) : List.of();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public Role getRole() {
        return role;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
