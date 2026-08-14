package com.fidecode.biteflow.domain.auth.entity;

import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity — User.
 * POJO puro, sem dependência de framework.
 */
public class User {

    private UUID id;
    private UUID storeId;
    private String name;
    private String login;
    private String passwordHash;
    private Role role;
    private List<Permission> permissions;
    private boolean active;

    public User(UUID storeId, String name, String login, String passwordHash,
                Role role, List<Permission> permissions) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório.");
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login é obrigatório.");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("passwordHash é obrigatório.");
        if (role == null) throw new IllegalArgumentException("role é obrigatório.");

        this.storeId = storeId;
        this.name = name;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
        this.permissions = permissions != null ? List.copyOf(permissions) : List.of();
        this.active = true;
    }

    /**
     * Factory method para criar usuário ADMIN com todas as permissões.
     */
    public static User createAdmin(UUID storeId, String name, String login, String passwordHash) {
        return new User(storeId, name, login, passwordHash, Role.ADMIN,
            Arrays.asList(Permission.values()));
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // ── Getters ──────────────────────────────────────

    public UUID getId()                     { return id; }
    public UUID getStoreId()               { return storeId; }
    public String getName()                { return name; }
    public String getLogin()               { return login; }
    public String getPasswordHash()        { return passwordHash; }
    public Role getRole()                  { return role; }
    public List<Permission> getPermissions(){ return permissions; }
    public boolean isActive()              { return active; }

    // ── Setters ──────────────────────────────────────

    public void setId(UUID id)                          { this.id = id; }
    public void setName(String name)                    { this.name = name; }
    public void setLogin(String login)                  { this.login = login; }
    public void setPasswordHash(String passwordHash)    { this.passwordHash = passwordHash; }
    public void setRole(Role role)                      { this.role = role; }
    public void setPermissions(List<Permission> perms)  { this.permissions = perms != null ? List.copyOf(perms) : List.of(); }
    public void setActive(boolean active)               { this.active = active; }
}
