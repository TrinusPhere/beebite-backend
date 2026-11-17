package com.trinusphere.beebite.domain.platform.entity;

import com.trinusphere.beebite.domain.platform.valueobject.BillingCycle;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class SaasContract {
    private UUID id;
    private final UUID tenantId;
    private String planName;
    private BillingCycle billingCycle;
    private BigDecimal monthlyFee;
    private BigDecimal setupFee;
    private int maxStores;
    private boolean includesIfood;
    private boolean includesEdgeSync;
    private int billingDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;
    private String contractNotes;
    private final Instant createdAt;
    private Instant updatedAt;

    public SaasContract(
            UUID tenantId,
            String planName,
            BillingCycle billingCycle,
            BigDecimal monthlyFee,
            BigDecimal setupFee,
            int maxStores,
            boolean includesIfood,
            boolean includesEdgeSync,
            int billingDay,
            LocalDate startDate,
            String contractNotes) {
        validate(tenantId, planName, monthlyFee, billingDay);
        this.tenantId = tenantId;
        this.planName = planName.trim();
        this.billingCycle = billingCycle != null ? billingCycle : BillingCycle.MONTHLY;
        this.monthlyFee = monthlyFee;
        this.setupFee = setupFee != null ? setupFee : BigDecimal.ZERO;
        this.maxStores = Math.max(1, maxStores);
        this.includesIfood = includesIfood;
        this.includesEdgeSync = includesEdgeSync;
        this.billingDay = billingDay;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.status = ContractStatus.ACTIVE;
        this.contractNotes = contractNotes;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private SaasContract(
            UUID id,
            UUID tenantId,
            String planName,
            BillingCycle billingCycle,
            BigDecimal monthlyFee,
            BigDecimal setupFee,
            int maxStores,
            boolean includesIfood,
            boolean includesEdgeSync,
            int billingDay,
            LocalDate startDate,
            LocalDate endDate,
            ContractStatus status,
            String contractNotes,
            Instant createdAt,
            Instant updatedAt) {
        validate(tenantId, planName, monthlyFee, billingDay);
        this.id = id;
        this.tenantId = tenantId;
        this.planName = planName.trim();
        this.billingCycle = billingCycle != null ? billingCycle : BillingCycle.MONTHLY;
        this.monthlyFee = monthlyFee;
        this.setupFee = setupFee != null ? setupFee : BigDecimal.ZERO;
        this.maxStores = Math.max(1, maxStores);
        this.includesIfood = includesIfood;
        this.includesEdgeSync = includesEdgeSync;
        this.billingDay = billingDay;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.endDate = endDate;
        this.status = status != null ? status : ContractStatus.ACTIVE;
        this.contractNotes = contractNotes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static SaasContract reconstitute(
            UUID id,
            UUID tenantId,
            String planName,
            BillingCycle billingCycle,
            BigDecimal monthlyFee,
            BigDecimal setupFee,
            int maxStores,
            boolean includesIfood,
            boolean includesEdgeSync,
            int billingDay,
            LocalDate startDate,
            LocalDate endDate,
            ContractStatus status,
            String contractNotes,
            Instant createdAt,
            Instant updatedAt) {
        return new SaasContract(
                id, tenantId, planName, billingCycle, monthlyFee, setupFee, maxStores, includesIfood, includesEdgeSync,
                billingDay, startDate, endDate, status, contractNotes, createdAt, updatedAt
        );
    }

    private static void validate(UUID tenantId, String planName, BigDecimal monthlyFee, int billingDay) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId é obrigatório.");
        }
        if (planName == null || planName.isBlank()) {
            throw new IllegalArgumentException("planName é obrigatório.");
        }
        if (monthlyFee == null || monthlyFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("monthlyFee deve ser maior ou igual a zero.");
        }
        if (billingDay < 1 || billingDay > 28) {
            throw new IllegalArgumentException("billingDay deve estar entre 1 e 28.");
        }
    }

    public void updateTerms(
            String planName,
            BigDecimal monthlyFee,
            int maxStores,
            boolean includesIfood,
            boolean includesEdgeSync,
            int billingDay,
            ContractStatus status,
            String contractNotes) {
        validate(this.tenantId, planName, monthlyFee, billingDay);
        this.planName = planName.trim();
        this.monthlyFee = monthlyFee;
        this.maxStores = Math.max(1, maxStores);
        this.includesIfood = includesIfood;
        this.includesEdgeSync = includesEdgeSync;
        this.billingDay = billingDay;
        if (status != null) {
            this.status = status;
        }
        this.contractNotes = contractNotes;
        this.updatedAt = Instant.now();
    }

    public void changeStatus(ContractStatus status) {
        if (status != null) {
            this.status = status;
            this.updatedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getPlanName() {
        return planName;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public BigDecimal getSetupFee() {
        return setupFee;
    }

    public int getMaxStores() {
        return maxStores;
    }

    public boolean isIncludesIfood() {
        return includesIfood;
    }

    public boolean isIncludesEdgeSync() {
        return includesEdgeSync;
    }

    public int getBillingDay() {
        return billingDay;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public String getContractNotes() {
        return contractNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

