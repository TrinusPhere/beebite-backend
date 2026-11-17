package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this(userRepository, null);
    }

    @Inject
    public DeleteUserUseCase(
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository) {
        this.userRepository = userRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    public void execute(UUID storeId, UUID id) {
        execute(storeId, null, id);
    }

    public void execute(UUID storeId, Role callerRole, UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        UserGovernanceSupport.ensureCallerCanManageTargetUser(user, callerRole);
        UserGovernanceSupport.ensureUserBelongsToStore(user, storeId, userStoreAccessRepository);
        userRepository.delete(id);
    }
}
