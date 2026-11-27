package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeReconciliationRunEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class EdgeReconciliationRunPanacheRepo implements PanacheRepositoryBase<EdgeReconciliationRunEntity, UUID> {
}

