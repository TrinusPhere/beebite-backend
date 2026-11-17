package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ListUsersUseCase {

    private final UserRepository userRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    public ListUsersUseCase(UserRepository userRepository) {
        this(userRepository, null);
    }

    @Inject
    public ListUsersUseCase(
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository) {
        this.userRepository = userRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    public List<UserResponse> execute(UUID storeId) {
        List<User> homeUsers = userRepository.findAllByStoreId(storeId);
        if (userStoreAccessRepository == null) {
            return homeUsers.stream()
                    .map(user -> UserGovernanceSupport.toUserResponse(user, storeId, null))
                    .toList();
        }

        Map<UUID, User> usersById = new LinkedHashMap<>();
        List<User> usersWithoutId = new ArrayList<>();

        for (User homeUser : homeUsers) {
            if (homeUser.getId() != null) {
                usersById.put(homeUser.getId(), homeUser);
            } else {
                usersWithoutId.add(homeUser);
            }
        }

        List<UserStoreAccess> storeAccesses = userStoreAccessRepository.findByStoreId(storeId);
        for (UserStoreAccess access : storeAccesses) {
            UUID userId = access.getUserId();
            if (userId != null && !usersById.containsKey(userId)) {
                userRepository.findById(userId).ifPresent(user -> usersById.put(userId, user));
            }
        }

        List<UserResponse> responses = new ArrayList<>();
        for (User user : usersById.values()) {
            responses.add(UserGovernanceSupport.toUserResponse(user, storeId, userStoreAccessRepository));
        }
        for (User user : usersWithoutId) {
            responses.add(UserGovernanceSupport.toUserResponse(user, storeId, userStoreAccessRepository));
        }
        return responses;
    }
}
