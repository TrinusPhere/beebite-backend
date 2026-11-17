package com.trinusphere.beebite.domain.platform;

import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.valueobject.BillingCycle;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;
import com.trinusphere.beebite.domain.platform.valueobject.InvoiceStatus;
import com.trinusphere.beebite.domain.platform.valueobject.TenantStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlatformDomainTest {

    @Test
    void shouldCreateAndManageTenantAccountLifecycle() {
        TenantAccount tenant = new TenantAccount(
                "Chefs Burguer Gastronomia Ltda",
                "Chefs Burguer",
                "João da Silva",
                "45.123.001/0001-10",
                "joao@chefsburguer.com.br",
                "(11) 99812-3344",
                "Cliente fundador Híbrido"
        );

        assertEquals("Chefs Burguer", tenant.getTradeName());
        assertEquals(TenantStatus.ACTIVE, tenant.getStatus());

        tenant.suspend();
        assertEquals(TenantStatus.SUSPENDED, tenant.getStatus());

        tenant.activate();
        assertEquals(TenantStatus.ACTIVE, tenant.getStatus());
    }

    @Test
    void shouldRejectTenantWithBlankNames() {
        assertThrows(IllegalArgumentException.class,
                () -> new TenantAccount("", "Fantasia", "Dono", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new TenantAccount("Razão", " ", "Dono", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new TenantAccount("Razão", "Fantasia", null, null, null, null, null));
    }

    @Test
    void shouldCreateValidSaasContract() {
        UUID tenantId = UUID.randomUUID();
        SaasContract contract = new SaasContract(
                tenantId,
                "HIBRIDO_MULTI",
                BillingCycle.MONTHLY,
                new BigDecimal("399.00"),
                new BigDecimal("299.00"),
                2,
                true,
                true,
                10,
                LocalDate.of(2026, 11, 1),
                "Contrato 2 lojas (Delivery + Balcão)"
        );

        assertEquals(tenantId, contract.getTenantId());
        assertEquals("HIBRIDO_MULTI", contract.getPlanName());
        assertEquals(new BigDecimal("399.00"), contract.getMonthlyFee());
        assertEquals(10, contract.getBillingDay());
        assertEquals(ContractStatus.ACTIVE, contract.getStatus());
    }

    @Test
    void shouldRejectInvalidBillingDayInContract() {
        UUID tenantId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> new SaasContract(
                        tenantId, "PLANO", BillingCycle.MONTHLY, BigDecimal.TEN, BigDecimal.ZERO,
                        1, true, false, 30, LocalDate.now(), null));
    }

    @Test
    void shouldCreateAndMarkSaasInvoicePaid() {
        UUID contractId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        SaasInvoice invoice = new SaasInvoice(
                contractId,
                tenantId,
                "2026-11",
                "Mensalidade HIBRIDO_MULTI - 11/2026",
                new BigDecimal("399.00"),
                LocalDate.of(2026, 11, 10)
        );

        assertEquals(InvoiceStatus.PENDING, invoice.getStatus());
        assertNull(invoice.getPaidAt());

        Instant paidMoment = Instant.now();
        invoice.markPaid("PIX", paidMoment, "Comprovante PIX recebido");

        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals("PIX", invoice.getPaymentMethod());
        assertEquals(paidMoment, invoice.getPaidAt());
        assertEquals("Comprovante PIX recebido", invoice.getNotes());
    }
}

