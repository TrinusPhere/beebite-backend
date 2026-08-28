package com.fidecode.biteflow.application.auth;

import com.fidecode.biteflow.domain.auth.exception.UserNotFoundException;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    @Inject
    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(UUID storeId, UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        StoreGuard.enforce(user, storeId);
        userRepository.delete(id);
    }
}
