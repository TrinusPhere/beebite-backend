package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.MarkInvoicePaidRequest;
import com.trinusphere.beebite.application.platform.dto.PlatformOverviewResponse;
import com.trinusphere.beebite.application.platform.dto.SaasInvoiceResponse;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.BillingCycle;
import com.trinusphere.beebite.domain.platform.valueobject.InvoiceStatus;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformBillingUseCaseTest {

    @Mock
    TenantAccountRepository tenantAccountRepository;
    @Mock
    StoreRepository storeRepository;
    @Mock
    SaasContractRepository saasContractRepository;
    @Mock
    SaasInvoiceRepository saasInvoiceRepository;

    private GetPlatformOverviewUseCase getPlatformOverviewUseCase;
    private GenerateMonthlyInvoicesUseCase generateMonthlyInvoicesUseCase;
    private MarkSaasInvoicePaidUseCase markSaasInvoicePaidUseCase;

    @BeforeEach
    void setUp() {
        getPlatformOverviewUseCase = new GetPlatformOverviewUseCase(
                tenantAccountRepository, storeRepository, saasContractRepository, saasInvoiceRepository);
        generateMonthlyInvoicesUseCase = new GenerateMonthlyInvoicesUseCase(
                saasContractRepository, saasInvoiceRepository, tenantAccountRepository);
        markSaasInvoicePaidUseCase = new MarkSaasInvoicePaidUseCase(
                saasInvoiceRepository, tenantAccountRepository);
    }

    @Test
    void shouldCalculatePlatformOverviewMrrAndInvoiceStats() {
        UUID tenantId = UUID.randomUUID();
        TenantAccount tenant = new TenantAccount(
                "Chefs Burguer Ltda", "Chefs Burguer", "João", null, null, null, null);
        tenant.setId(tenantId);

        Store cloudStore = Store.reconstitute(
                UUID.randomUUID(), "Delivery", null, true, null, null, null, "CLOUD", null, null, tenantId, null, true);
        Store edgeStore = Store.reconstitute(
                UUID.randomUUID(), "Balcão", null, true, null, null, null, "EDGE", "tok", null, tenantId, null, true);

        SaasContract contract = new SaasContract(
                tenantId, "HIBRIDO", BillingCycle.MONTHLY, new BigDecimal("399.00"), BigDecimal.ZERO,
                2, true, true, 10, LocalDate.now(), null);
        contract.setId(UUID.randomUUID());

        SaasInvoice paidInvoice = new SaasInvoice(
                contract.getId(), tenantId, "2026-11", "Mensalidade 11/2026",
                new BigDecimal("399.00"), LocalDate.now());
        paidInvoice.markPaid("PIX", null, null);

        SaasInvoice overdueInvoice = new SaasInvoice(
                contract.getId(), tenantId, "2026-10", "Mensalidade 10/2026",
                new BigDecimal("199.00"), LocalDate.now().minusDays(10));
        overdueInvoice.markOverdue();

        when(tenantAccountRepository.findAll()).thenReturn(List.of(tenant));
        when(storeRepository.findAll()).thenReturn(List.of(cloudStore, edgeStore));
        when(saasContractRepository.findAll()).thenReturn(List.of(contract));
        when(saasInvoiceRepository.findAll()).thenReturn(List.of(paidInvoice, overdueInvoice));

        PlatformOverviewResponse overview = getPlatformOverviewUseCase.execute();

        assertEquals(1, overview.totalTenants());
        assertEquals(1, overview.activeTenants());
        assertEquals(2, overview.totalStores());
        assertEquals(1, overview.cloudStores());
        assertEquals(1, overview.edgeStores());
        assertEquals(new BigDecimal("399.00"), overview.mrrAmount());
        assertEquals(new BigDecimal("399.00"), overview.receivedThisMonth());
        assertEquals(new BigDecimal("199.00"), overview.overdueAmount());
        assertEquals(1, overview.overdueInvoicesCount());
    }

    @Test
    void shouldGenerateMonthlyInvoicesIdempotentlyForActiveContracts() {
        UUID tenantId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();
        TenantAccount tenant = new TenantAccount(
                "Chefs Burguer Ltda", "Chefs Burguer", "João", null, null, null, null);
        tenant.setId(tenantId);

        SaasContract contract = new SaasContract(
                tenantId, "HIBRIDO_MULTI", BillingCycle.MONTHLY, new BigDecimal("399.00"), BigDecimal.ZERO,
                2, true, true, 10, LocalDate.of(2026, 11, 1), null);
        contract.setId(contractId);

        when(saasContractRepository.findAll()).thenReturn(List.of(contract));
        when(saasInvoiceRepository.findByContractAndMonth(contractId, "2026-12")).thenReturn(Optional.empty());
        when(tenantAccountRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(saasInvoiceRepository.save(any(SaasInvoice.class))).thenAnswer(inv -> {
            SaasInvoice i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        List<SaasInvoiceResponse> generated = generateMonthlyInvoicesUseCase.execute("2026-12");

        assertEquals(1, generated.size());
        assertEquals("2026-12", generated.get(0).referenceMonth());
        assertEquals(new BigDecimal("399.00"), generated.get(0).amount());
        assertEquals(LocalDate.of(2026, 12, 10), generated.get(0).dueDate());
    }

    @Test
    void shouldMarkInvoiceAsPaid() {
        UUID invoiceId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        SaasInvoice invoice = new SaasInvoice(
                UUID.randomUUID(), tenantId, "2026-11", "Mensalidade", new BigDecimal("399.00"), LocalDate.now());
        invoice.setId(invoiceId);

        when(saasInvoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(saasInvoiceRepository.save(any(SaasInvoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tenantAccountRepository.findById(tenantId)).thenReturn(Optional.empty());

        SaasInvoiceResponse paid = markSaasInvoicePaidUseCase.execute(
                invoiceId, new MarkInvoicePaidRequest("PIX", "Pago via chave CNPJ"));

        assertEquals(InvoiceStatus.PAID.name(), paid.status());
        assertEquals("PIX", paid.paymentMethod());
        assertNotNull(paid.paidAt());
    }
}

