package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.auth.ListAccessibleStoresUseCase;
import com.trinusphere.beebite.application.auth.SwitchStoreUseCase;
import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.application.auth.dto.LoginResponse;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.auth.port.TokenProvider;
import com.trinusphere.beebite.application.platform.dto.ChangeTenantStatusRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.TenantStatus;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformOnboardingUseCaseTest {

    @Mock
    TenantAccountRepository tenantAccountRepository;
    @Mock
    StoreRepository storeRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    UserStoreAccessRepository userStoreAccessRepository;
    @Mock
    SaasContractRepository saasContractRepository;
    @Mock
    SaasInvoiceRepository saasInvoiceRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    TokenProvider tokenProvider;

    private OnboardClientTenantUseCase onboardClientTenantUseCase;
    private ChangeTenantStatusUseCase changeTenantStatusUseCase;
    private ListAccessibleStoresUseCase listAccessibleStoresUseCase;
    private SwitchStoreUseCase switchStoreUseCase;

    @BeforeEach
    void setUp() {
        onboardClientTenantUseCase = new OnboardClientTenantUseCase(
                tenantAccountRepository,
                storeRepository,
                userRepository,
                userStoreAccessRepository,
                saasContractRepository,
                saasInvoiceRepository,
                passwordEncoder
        );
        changeTenantStatusUseCase = new ChangeTenantStatusUseCase(
                tenantAccountRepository,
                storeRepository,
                saasContractRepository
        );
        listAccessibleStoresUseCase = new ListAccessibleStoresUseCase(
                userRepository,
                userStoreAccessRepository,
                storeRepository
        );
        switchStoreUseCase = new SwitchStoreUseCase(
                userRepository,
                userStoreAccessRepository,
                tokenProvider,
                listAccessibleStoresUseCase,
                storeRepository
        );
    }

    @Test
    void shouldOnboardNewClientWithOwnerUserTwoStoresEdgeTokenAndContract() {
        OnboardClientRequest request = new OnboardClientRequest(
                "Chefs Burguer Gastronomia Ltda",
                "Chefs Burguer",
                "João da Silva",
                "45.123.001/0001-10",
                "joao@chefsburguer.com.br",
                "(11) 99812-3344",
                "chefsburguer",
                "Chefs@2026!",
                List.of(
                        new OnboardClientRequest.StoreSetupDto(
                                "Chefs Burguer - Delivery", "45.123.001/0001-10", "Rua Delivery, 10", "chefs-delivery", "CLOUD"),
                        new OnboardClientRequest.StoreSetupDto(
                                "Chefs Burguer - Balcão", "45.123.002/0001-88", "Av. Central, 200", "chefs-balcao", "EDGE")
                ),
                new OnboardClientRequest.ContractSetupDto(
                        "HIBRIDO_MULTI",
                        new BigDecimal("399.00"),
                        new BigDecimal("299.00"),
                        10,
                        true,
                        true
                )
        );

        when(userRepository.existsByLogin("chefsburguer")).thenReturn(false);
        when(passwordEncoder.hash("Chefs@2026!")).thenReturn("hashed_pass");
        when(tenantAccountRepository.save(any(TenantAccount.class))).thenAnswer(inv -> {
            TenantAccount t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> {
            Store s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(UUID.randomUUID());
            }
            return s;
        });
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(userStoreAccessRepository.save(any(UserStoreAccess.class))).thenAnswer(inv -> inv.getArgument(0));
        when(saasContractRepository.save(any(SaasContract.class))).thenAnswer(inv -> {
            SaasContract c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });
        when(saasInvoiceRepository.save(any(SaasInvoice.class))).thenAnswer(inv -> inv.getArgument(0));

        OnboardClientResponse response = onboardClientTenantUseCase.execute(request);

        assertNotNull(response.tenantId());
        assertEquals("Chefs Burguer", response.tradeName());
        assertEquals("chefsburguer", response.ownerLogin());
        assertNotNull(response.contractId());
        assertEquals(2, response.stores().size());

        var cloudStore = response.stores().get(0);
        var edgeStore = response.stores().get(1);

        assertEquals("CLOUD", cloudStore.syncMode());
        assertNull(cloudStore.generatedEdgeDeviceToken());

        assertEquals("EDGE", edgeStore.syncMode());
        assertNotNull(edgeStore.generatedEdgeDeviceToken());
        assertTrue(edgeStore.generatedEdgeDeviceToken().startsWith("bf_edge_"));

        verify(userStoreAccessRepository, times(2)).save(any(UserStoreAccess.class));
        verify(saasContractRepository, times(1)).save(any(SaasContract.class));
        verify(saasInvoiceRepository, times(2)).save(any(SaasInvoice.class)); // Setup + 1a Mensalidade
    }

    @Test
    void shouldAllowSuperAdminToListAndSwitchToAnyStoreWithoutManualUserStoreAccess() {
        UUID superAdminId = UUID.randomUUID();
        UUID store1Id = UUID.randomUUID();
        UUID store2Id = UUID.randomUUID();

        User superAdmin = new User(
                store1Id,
                "Admin Trinusphere",
                "admin",
                "hash",
                Role.SUPER_ADMIN,
                Arrays.asList(Permission.values())
        );
        superAdmin.setId(superAdminId);

        Store store1 = Store.reconstitute(store1Id, "Loja Cliente A", null, true, null, null, null);
        Store store2 = Store.reconstitute(store2Id, "Loja Cliente B (Balcão)", null, true, null, null, null,
                "EDGE", "token_hash", null);

        when(userRepository.findById(superAdminId)).thenReturn(Optional.of(superAdmin));
        when(storeRepository.findAll()).thenReturn(List.of(store1, store2));
        when(storeRepository.findById(store2Id)).thenReturn(Optional.of(store2));
        when(tokenProvider.generateTokenForStore(any(), eq(store2Id), eq(Role.SUPER_ADMIN), any()))
                .thenReturn("super.admin.jwt.for.store2");

        List<AccessibleStoreResponse> stores = listAccessibleStoresUseCase.execute(superAdminId, store1Id);
        assertEquals(2, stores.size());
        assertEquals("SUPER_ADMIN", stores.get(0).role());

        LoginResponse switched = switchStoreUseCase.execute(superAdminId, store2Id);
        assertEquals("super.admin.jwt.for.store2", switched.token());
        assertEquals(store2Id.toString(), switched.user().storeId());
        assertEquals("SUPER_ADMIN", switched.user().role());
        verify(userStoreAccessRepository, never()).findByUserAndStore(any(), any());
    }

    @Test
    void shouldSuspendTenantAndDeactivateItsStores() {
        UUID tenantId = UUID.randomUUID();
        TenantAccount tenant = new TenantAccount(
                "Empresa X Ltda", "Lanchonete X", "Dono X", null, null, null, null);
        tenant.setId(tenantId);

        Store store1 = Store.reconstitute(
                UUID.randomUUID(), "Lanchonete X - Unidade 1", null, true, null, null, null,
                "CLOUD", null, null, tenantId, "11.111.111/0001-11", true);

        when(tenantAccountRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(storeRepository.findAll()).thenReturn(List.of(store1));
        when(saasContractRepository.findByTenantId(tenantId)).thenReturn(List.of());

        changeTenantStatusUseCase.execute(tenantId, new ChangeTenantStatusRequest("SUSPENDED"));

        assertEquals(TenantStatus.SUSPENDED, tenant.getStatus());
        assertFalse(store1.isActive());
        assertFalse(store1.isOpen());
        verify(storeRepository).save(store1);
    }
}

