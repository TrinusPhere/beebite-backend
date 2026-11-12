package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.infrastructure.persistence.panache.entity.CashSessionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CashSessionPanacheRepo implements PanacheRepositoryBase<CashSessionEntity, UUID> {
}
