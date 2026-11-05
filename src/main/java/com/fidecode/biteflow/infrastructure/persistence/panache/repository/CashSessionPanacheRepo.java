package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.infrastructure.persistence.panache.entity.CashSessionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CashSessionPanacheRepo implements PanacheRepositoryBase<CashSessionEntity, UUID> {
}
