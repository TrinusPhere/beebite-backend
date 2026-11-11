package com.fidecode.biteflow.application.auth.dto;

import java.util.List;

public record LoginResponse(
        String token,
        long expiresIn,
        UserSummary user,
        List<AccessibleStoreResponse> accessibleStores
) {
    public LoginResponse(String token, long expiresIn, UserSummary user) {
        this(token, expiresIn, user, List.of());
    }

    public record UserSummary(
            String id,
            String name,
            String login,
            String role,
            List<String> permissions,
            String storeId
    ) {
    }
}
