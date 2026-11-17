package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.valueobject.TenantStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.TenantAccountEntity;

public final class TenantAccountMapper {

    private TenantAccountMapper() {
    }

    public static TenantAccount toDomain(TenantAccountEntity e) {
        if (e == null) {
            return null;
        }
        return TenantAccount.reconstitute(
                e.id,
                e.companyName,
                e.tradeName,
                e.ownerName,
                e.documentNumber,
                e.email,
                e.phone,
                e.status != null ? TenantStatus.valueOf(e.status) : TenantStatus.ACTIVE,
                e.notes,
                e.createdAt,
                e.updatedAt
        );
    }

    public static TenantAccountEntity toEntity(TenantAccount d) {
        if (d == null) {
            return null;
        }
        TenantAccountEntity e = new TenantAccountEntity();
        e.id = d.getId();
        e.companyName = d.getCompanyName();
        e.tradeName = d.getTradeName();
        e.ownerName = d.getOwnerName();
        e.documentNumber = d.getDocumentNumber();
        e.email = d.getEmail();
        e.phone = d.getPhone();
        e.status = d.getStatus().name();
        e.notes = d.getNotes();
        e.createdAt = d.getCreatedAt();
        e.updatedAt = d.getUpdatedAt();
        return e;
    }
}

