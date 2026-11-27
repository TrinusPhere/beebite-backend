package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeSyncStreamStateEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class EdgeSyncStreamStatePanacheRepo implements PanacheRepositoryBase<EdgeSyncStreamStateEntity, UUID> {
}

