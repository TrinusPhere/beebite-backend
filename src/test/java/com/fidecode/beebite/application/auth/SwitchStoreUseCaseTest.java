package com.fidecode.beebite.application.auth;

import com.fidecode.beebite.application.auth.dto.AccessibleStoreResponse;
import com.fidecode.beebite.application.auth.dto.GrantUserStoreAccessRequest;
import com.fidecode.beebite.application.auth.dto.LoginResponse;
import com.fidecode.beebite.application.auth.port.TokenProvider;
import com.fidecode.beebite.domain.auth.entity.User;
import com.fidecode.beebite.domain.auth.entity.UserStoreAccess;
import com.fidecode.beebite.domain.auth.exception.InvalidCredentialsException;
import com.fidecode.beebite.domain.auth.repository.UserRepository;
import com.fidecode.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.fidecode.beebite.domain.auth.valueobject.Permission;
import com.fidecode.beebite.domain.auth.valueobject.Role;
import com.fidecode.beebite.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.beebite.domain.store.entity.Store;
import com.fidecode.beebite.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwitchStoreUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    UserStoreAccessRepository userStoreAccessRepository;
    @Mock
    StoreRepository storeRepository;
    @Mock
    TokenProvider tokenProvider;

    private ListAccessibleStoresUseCase listAccessibleStoresUseCase;
    private SwitchStoreUseCase switchStoreUseCase;
    private GrantUserStoreAccessUseCase grantUserStoreAccessUseCase;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final UUID STORE_DELIVERY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID STORE_BALCAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private User ownerUser;
    private Store deliveryStore;
    private Store balcaoStore;

    @BeforeEach
    void setUp() {
        listAccessibleStoresUseCase = new ListAccessibleStoresUseCase(
                userRepository, userStoreAccessRepository, storeRepository);
        switchStoreUseCase = new SwitchStoreUseCase(
                userRepository, userStoreAccessRepository, tokenProvider, listAccessibleStoresUseCase);
        grantUserStoreAccessUseCase = new GrantUserStoreAccessUseCase(
                userRepository, storeRepository, userStoreAccessRepository);

        ownerUser = User.createAdmin(STORE_DELIVERY_ID, "Proprietário Chefs", "dono", "hash");
        ownerUser.setId(USER_ID);

        deliveryStore = Store.reconstitute(
                STORE_DELIVERY_ID, "Chefs Burguer - Delivery", null, true, null, "Rua Delivery", "chefs-delivery",
                "CLOUD", null, null);
        balcaoStore = Store.reconstitute(
                STORE_BALCAO_ID, "Chefs Burguer - Balcão", null, true, null, "Av Balcão", "chefs-balcao",
                "EDGE", "edge_hash", Instant.parse("2026-11-11T03:00:00Z"));
    }

    @Test
    void shouldSwitchStoreWithoutLoggingOutWhenUserHasAccess() {
        UserStoreAccess deliveryAccess = new UserStoreAccess(
                USER_ID, STORE_DELIVERY_ID, Role.ADMIN, List.of(Permission.DASHBOARD, Permission.PEDIDOS));
        UserStoreAccess balcaoAccess = new UserStoreAccess(
                USER_ID, STORE_BALCAO_ID, Role.ADMIN, List.of(Permission.DASHBOARD, Permission.PEDIDOS, Permission.FINANCEIRO));

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(ownerUser));
        when(userStoreAccessRepository.findByUserAndStore(USER_ID, STORE_BALCAO_ID))
                .thenReturn(Optional.of(balcaoAccess));
        when(userStoreAccessRepository.findByUserId(USER_ID))
                .thenReturn(List.of(deliveryAccess, balcaoAccess));
        when(storeRepository.findById(STORE_DELIVERY_ID)).thenReturn(Optional.of(deliveryStore));
        when(storeRepository.findById(STORE_BALCAO_ID)).thenReturn(Optional.of(balcaoStore));
        when(tokenProvider.generateTokenForStore(
                eq(ownerUser), eq(STORE_BALCAO_ID), eq(Role.ADMIN), eq(balcaoAccess.getPermissions())))
                .thenReturn("new.jwt.for.balcao");

        LoginResponse response = switchStoreUseCase.execute(USER_ID, STORE_BALCAO_ID);

        assertEquals("new.jwt.for.balcao", response.token());
        assertEquals(STORE_BALCAO_ID.toString(), response.user().storeId());
        assertEquals(2, response.accessibleStores().size());

        AccessibleStoreResponse activeStore = response.accessibleStores().stream()
                .filter(AccessibleStoreResponse::activeContext)
                .findFirst()
                .orElseThrow();
        assertEquals(STORE_BALCAO_ID.toString(), activeStore.storeId());
        assertEquals("Chefs Burguer - Balcão", activeStore.storeName());
        assertEquals("EDGE", activeStore.syncMode());
        assertNotNull(activeStore.lastEdgeSyncAt());
    }

    @Test
    void shouldRejectSwitchWhenUserDoesNotHaveAccessToTargetStore() {
        UUID unauthorizedStoreId = UUID.randomUUID();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(ownerUser));
        when(userStoreAccessRepository.findByUserAndStore(USER_ID, unauthorizedStoreId))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenAccessException.class,
                () -> switchStoreUseCase.execute(USER_ID, unauthorizedStoreId));
        verify(tokenProvider, never()).generateTokenForStore(any(), any(), any(), any());
    }

    @Test
    void shouldRejectSwitchWhenUserIsInactive() {
        ownerUser.deactivate();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(ownerUser));

        assertThrows(InvalidCredentialsException.class,
                () -> switchStoreUseCase.execute(USER_ID, STORE_BALCAO_ID));
    }

    @Test
    void shouldGrantAccessToSecondaryStoreForExistingUser() {
        GrantUserStoreAccessRequest request = new GrantUserStoreAccessRequest(
                USER_ID.toString(),
                STORE_BALCAO_ID.toString(),
                "ADMIN",
                List.of("dashboard", "pedidos", "financeiro")
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(ownerUser));
        when(storeRepository.findById(STORE_BALCAO_ID)).thenReturn(Optional.of(balcaoStore));
        when(userStoreAccessRepository.findByUserAndStore(USER_ID, STORE_BALCAO_ID)).thenReturn(Optional.empty());
        when(userStoreAccessRepository.save(any(UserStoreAccess.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessibleStoreResponse granted = grantUserStoreAccessUseCase.execute(request);

        assertEquals(STORE_BALCAO_ID.toString(), granted.storeId());
        assertEquals("Chefs Burguer - Balcão", granted.storeName());
        assertEquals("ADMIN", granted.role());
        assertEquals(3, granted.permissions().size());
        verify(userStoreAccessRepository).save(any(UserStoreAccess.class));
    }
}
