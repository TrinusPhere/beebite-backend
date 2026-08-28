package com.fidecode.biteflow.domain.auth.entity;

import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class User implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private String name;
    private final String login;
    private String passwordHash;
    private Role role;
    private List<Permission> permissions;
    private boolean active;

    public User(UUID storeId, String name, String login, String passwordHash, Role role, List<Permission> permissions) {
        validate(storeId, name, login, passwordHash, role);
        this.storeId = storeId;
        this.name = name;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
        this.permissions = permissions != null ? List.copyOf(permissions) : List.of();
        this.active = true;
    }

    public static User reconstitute(UUID id, UUID storeId, String name, String login, String passwordHash, Role role,
            List<Permission> permissions, boolean active) {
        User u = new User(storeId, name, login, passwordHash, role, permissions);
        u.id = id;
        u.active = active;
        return u;
    }

    public static User createAdmin(UUID storeId, String name, String login, String passwordHash) {
        return new User(storeId, name, login, passwordHash, Role.ADMIN, Arrays.asList(Permission.values()));
    }

    private static void validate(UUID storeId, String name, String login, String hash, Role role) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        if (login == null || login.isBlank())
            throw new IllegalArgumentException("login é obrigatório.");
        if (hash == null || hash.isBlank())
            throw new IllegalArgumentException("passwordHash é obrigatório.");
        if (role == null)
            throw new IllegalArgumentException("role é obrigatório.");
    }

    public void rename(String name) {
        if (name != null && !name.isBlank())
            this.name = name;
    }

    public void changePasswordHash(String hash) {
        if (hash == null || hash.isBlank())
            throw new IllegalArgumentException("passwordHash é obrigatório.");
        this.passwordHash = hash;
    }

    public void changeRole(Role role) {
        if (role == null)
            throw new IllegalArgumentException("role é obrigatório.");
        this.role = role;
    }

    public void replacePermissions(List<Permission> permissions) {
        this.permissions = permissions != null ? List.copyOf(permissions) : List.of();
    }

    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}