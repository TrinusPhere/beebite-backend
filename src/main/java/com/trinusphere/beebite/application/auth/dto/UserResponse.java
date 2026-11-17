package com.trinusphere.beebite.application.auth.dto;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        String id,
        String name,
        String login,
        String role,
        List<String> permissions,
        boolean active,
        Instant createdAt,
        String storeId,
        List<String> storeIds
) {
    public UserResponse(String id, String name, String login, String role, List<String> permissions, boolean active,
            Instant createdAt, String storeId) {
        this(id, name, login, role, permissions, active, createdAt, storeId,
                storeId != null ? List.of(storeId) : List.of());
    }
}
