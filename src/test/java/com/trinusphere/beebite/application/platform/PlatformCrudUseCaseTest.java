package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.CreateSaasInvoiceRequest;
import com.trinusphere.beebite.application.platform.dto.SaasContractResponse;
import com.trinusphere.beebite.application.platform.dto.SaasInvoiceResponse;
import com.trinusphere.beebite.application.platform.dto.TenantAccountResponse;
import com.trinusphere.beebite.application.platform.dto.UpdatePlatformStoreRequest;
import com.trinusphere.beebite.application.platform.dto.UpdateSaasContractRequest;
import com.trinusphere.beebite.application.platform.dto.UpdateTenantAccountRequest;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.BillingCycle;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;
import com.trinusphere.beebite.domain.platform.valueobject.InvoiceStatus;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformCrudUseCaseTest {

    @Mock
    StoreRepository storeRepository;
    @Mock
    TenantAccountRepository tenantAccountRepository;
    @Mock
    SaasContractRepository saasContractRepository;
    @Mock
    SaasInvoiceRepository saasInvoiceRepository;

    private UpdatePlatformStoreUseCase updatePlatformStoreUseCase;
    private RegenerateEdgeTokenUseCase regenerateEdgeTokenUseCase;
    private UpdateTenantAccountUseCase updateTenantAccountUseCase;
    private UpdateSaasContractUseCase updateSaasContractUseCase;
    private CreateSaasInvoiceUseCase createSaasInvoiceUseCase;
    private CancelSaasInvoiceUseCase cancelSaasInvoiceUseCase;

    @BeforeEach
    void setUp() {
        updatePlatformStoreUseCase = new UpdatePlatformStoreUseCase(storeRepository);
        regenerateEdgeTokenUseCase = new RegenerateEdgeTokenUseCase(storeRepository);
        updateTenantAccountUseCase = new UpdateTenantAccountUseCase(tenantAccountRepository);
        updateSaasContractUseCase = new UpdateSaasContractUseCase(saasContractRepository, tenantAccountRepository);
        createSaasInvoiceUseCase = new CreateSaasInvoiceUseCase(
                saasInvoiceRepository, saasContractRepository, tenantAccountRepository);
        cancelSaasInvoiceUseCase = new CancelSaasInvoiceUseCase(saasInvoiceRepository, tenantAccountRepository);
    }

    @Test
    void shouldUpdateStoreMetadataAndGenerateEdgeTokenWhenSwitchingFromCloudToEdge() {
        UUID storeId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Store store = Store.reconstitute(
                storeId, "Loja Antiga", null, true, null, "Rua A", "loja-antiga",
                "CLOUD", null, null, tenantId, "11.111.111/0001-11", true);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantAccountResponse.StoreSummaryDto updated = updatePlatformStoreUseCase.execute(
                storeId,
                new UpdatePlatformStoreRequest(
                        "Chefs Burguer - Balcão",
                        "45.123.002/0001-88",
                        "Av. Central, 500",
                        "chefs-balcao",
                        "EDGE",
                        true
                )
        );

        assertEquals("Chefs Burguer - Balcão", updated.name());
        assertEquals("45.123.002/0001-88", updated.cnpj());
        assertEquals("Av. Central, 500", updated.address());
        assertEquals("chefs-balcao", updated.publicSlug());
        assertEquals("EDGE", updated.syncMode());
        assertTrue(updated.active());
        assertNotNull(updated.edgeDeviceToken());
        assertTrue(updated.edgeDeviceToken().startsWith("bf_edge_"));
    }

    @Test
    void shouldPreserveExistingEdgeTokenWhenUpdatingEdgeStoreInfo() {
        UUID storeId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Store store = Store.reconstitute(
                storeId, "Balcão", null, true, null, "Rua 1", "balcao",
                "EDGE", "bf_edge_existing_token", null, tenantId, "11.111.111/0001-11", true);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantAccountResponse.StoreSummaryDto updated = updatePlatformStoreUseCase.execute(
                storeId,
                new UpdatePlatformStoreRequest("Balcão Atualizado", "22.222.222/0001-22", "Rua 2", "balcao-2", "EDGE", true)
        );

        assertEquals("bf_edge_existing_token", updated.edgeDeviceToken());
    }

    @Test
    void shouldThrowStoreNotFoundWhenUpdatingUnknownStore() {
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> updatePlatformStoreUseCase.execute(
                        storeId,
                        new UpdatePlatformStoreRequest("Nome", null, null, null, "CLOUD", true)));
    }

    @Test
    void shouldRegenerateEdgeToken() {
        UUID storeId = UUID.randomUUID();
        Store store = Store.reconstitute(
                storeId, "Balcão", null, true, null, "Rua 1", "balcao",
                "EDGE", "bf_edge_old", null, UUID.randomUUID(), null, true);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantAccountResponse.StoreSummaryDto res = regenerateEdgeTokenUseCase.execute(storeId);

        assertNotEquals("bf_edge_old", res.edgeDeviceToken());
        assertTrue(res.edgeDeviceToken().startsWith("bf_edge_"));
        assertEquals("EDGE", res.syncMode());
    }

    @Test
    void shouldUpdateTenantAccountInfo() {
        UUID tenantId = UUID.randomUUID();
        TenantAccount tenant = new TenantAccount(
                "Antiga Ltda", "Antiga", "Pedro", "111", "a@a.com", "11999", null);
        tenant.setId(tenantId);

        when(tenantAccountRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantAccountRepository.save(any(TenantAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        updateTenantAccountUseCase.execute(
                tenantId,
                new UpdateTenantAccountRequest(
                        "Chefs Burguer Alimentos Ltda",
                        "Chefs Burguer",
                        "João Silva",
                        "45.123.001/0001-10",
                        "joao@chefsburguer.com.br",
                        "(11) 99888-7766",
                        "Cliente VIP"
                )
        );

        assertEquals("Chefs Burguer", tenant.getTradeName());
        assertEquals("João Silva", tenant.getOwnerName());
        assertEquals("Cliente VIP", tenant.getNotes());
    }

    @Test
    void shouldUpdateSaasContractTerms() {
        UUID tenantId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();
        TenantAccount tenant = new TenantAccount("Emp Ltda", "Emp", "Dono", null, null, null, null);
        tenant.setId(tenantId);

        SaasContract contract = new SaasContract(
                tenantId, "ESSENCIAL", BillingCycle.MONTHLY, new BigDecimal("199.00"), BigDecimal.ZERO,
                1, true, false, 10, LocalDate.now(), null);
        contract.setId(contractId);

        when(saasContractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(saasContractRepository.save(any(SaasContract.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tenantAccountRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        SaasContractResponse res = updateSaasContractUseCase.execute(
                contractId,
                new UpdateSaasContractRequest(
                        "HIBRIDO_MULTI",
                        "MONTHLY",
                        new BigDecimal("449.00"),
                        3,
                        true,
                        true,
                        15,
                        "ACTIVE",
                        "Upgrade para incluir PDV Balcão"
                )
        );

        assertEquals("HIBRIDO_MULTI", res.planName());
        assertEquals(new BigDecimal("449.00"), res.monthlyFee());
        assertEquals(3, res.maxStores());
        assertEquals(15, res.billingDay());
        assertEquals(ContractStatus.ACTIVE.name(), res.status());
    }

    @Test
    void shouldCreateAndCancelSaasInvoice() {
        UUID tenantId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        TenantAccount tenant = new TenantAccount("Emp Ltda", "Chefs Burguer", "Dono", null, null, null, null);
        tenant.setId(tenantId);
        SaasContract contract = new SaasContract(
                tenantId, "HIBRIDO", BillingCycle.MONTHLY, new BigDecimal("399.00"), BigDecimal.ZERO,
                2, true, true, 10, LocalDate.now(), null);
        contract.setId(contractId);

        when(tenantAccountRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(saasContractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(saasInvoiceRepository.save(any(SaasInvoice.class))).thenAnswer(inv -> {
            SaasInvoice i = inv.getArgument(0);
            if (i.getId() == null) {
                i.setId(invoiceId);
            }
            return i;
        });

        SaasInvoiceResponse created = createSaasInvoiceUseCase.execute(
                new CreateSaasInvoiceRequest(
                        tenantId,
                        contractId,
                        "2026-11",
                        "Treinamento Presencial Equipe Balcão",
                        new BigDecimal("250.00"),
                        LocalDate.of(2026, 11, 25)
                )
        );

        assertEquals(invoiceId.toString(), created.id());
        assertEquals("Chefs Burguer", created.tenantTradeName());
        assertEquals(new BigDecimal("250.00"), created.amount());

        SaasInvoice existing = new SaasInvoice(
                contractId, tenantId, "2026-11", "Cobrança duplicada", new BigDecimal("250.00"), LocalDate.now());
        existing.setId(invoiceId);
        when(saasInvoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existing));

        SaasInvoiceResponse cancelled = cancelSaasInvoiceUseCase.execute(invoiceId, "Emitida em duplicidade");
        assertEquals(InvoiceStatus.CANCELLED.name(), cancelled.status());
    }
}

