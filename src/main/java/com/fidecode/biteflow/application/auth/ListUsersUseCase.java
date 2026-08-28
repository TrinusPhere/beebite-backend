package com.fidecode.biteflow.application.auth;

import com.fidecode.biteflow.application.auth.dto.UserResponse;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ListUsersUseCase {

    private final UserRepository userRepository;

    @Inject
    public ListUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> execute(UUID storeId) {
        return userRepository.findAllByStoreId(storeId).stream()
            .map(CreateUserUseCase::toResponse)
            .toList();
    }
}
