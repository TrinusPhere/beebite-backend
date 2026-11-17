package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.valueobject.InvoiceStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.SaasInvoiceEntity;

public final class SaasInvoiceMapper {

    private SaasInvoiceMapper() {
    }

    public static SaasInvoice toDomain(SaasInvoiceEntity e) {
        if (e == null) {
            return null;
        }
        return SaasInvoice.reconstitute(
                e.id,
                e.contractId,
                e.tenantId,
                e.referenceMonth,
                e.description,
                e.amount,
                e.dueDate,
                e.paidAt,
                e.paymentMethod,
                e.status != null ? InvoiceStatus.valueOf(e.status) : InvoiceStatus.PENDING,
                e.notes,
                e.createdAt,
                e.updatedAt
        );
    }

    public static SaasInvoiceEntity toEntity(SaasInvoice d) {
        if (d == null) {
            return null;
        }
        SaasInvoiceEntity e = new SaasInvoiceEntity();
        e.id = d.getId();
        e.contractId = d.getContractId();
        e.tenantId = d.getTenantId();
        e.referenceMonth = d.getReferenceMonth();
        e.description = d.getDescription();
        e.amount = d.getAmount();
        e.dueDate = d.getDueDate();
        e.paidAt = d.getPaidAt();
        e.paymentMethod = d.getPaymentMethod();
        e.status = d.getStatus().name();
        e.notes = d.getNotes();
        e.createdAt = d.getCreatedAt();
        e.updatedAt = d.getUpdatedAt();
        return e;
    }
}

