package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.valueobject.BillingCycle;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.SaasContractEntity;

public final class SaasContractMapper {

    private SaasContractMapper() {
    }

    public static SaasContract toDomain(SaasContractEntity e) {
        if (e == null) {
            return null;
        }
        return SaasContract.reconstitute(
                e.id,
                e.tenantId,
                e.planName,
                e.billingCycle != null ? BillingCycle.valueOf(e.billingCycle) : BillingCycle.MONTHLY,
                e.monthlyFee,
                e.setupFee,
                e.maxStores,
                e.includesIfood,
                e.includesEdgeSync,
                e.billingDay,
                e.startDate,
                e.endDate,
                e.status != null ? ContractStatus.valueOf(e.status) : ContractStatus.ACTIVE,
                e.contractNotes,
                e.createdAt,
                e.updatedAt
        );
    }

    public static SaasContractEntity toEntity(SaasContract d) {
        if (d == null) {
            return null;
        }
        SaasContractEntity e = new SaasContractEntity();
        e.id = d.getId();
        e.tenantId = d.getTenantId();
        e.planName = d.getPlanName();
        e.billingCycle = d.getBillingCycle().name();
        e.monthlyFee = d.getMonthlyFee();
        e.setupFee = d.getSetupFee();
        e.maxStores = d.getMaxStores();
        e.includesIfood = d.isIncludesIfood();
        e.includesEdgeSync = d.isIncludesEdgeSync();
        e.billingDay = d.getBillingDay();
        e.startDate = d.getStartDate();
        e.endDate = d.getEndDate();
        e.status = d.getStatus().name();
        e.contractNotes = d.getContractNotes();
        e.createdAt = d.getCreatedAt();
        e.updatedAt = d.getUpdatedAt();
        return e;
    }
}

