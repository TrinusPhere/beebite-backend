package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.infrastructure.persistence.panache.entity.EdgeOutboxEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class EdgeOutboxPanacheRepo implements PanacheRepositoryBase<EdgeOutboxEntity, UUID> {
}
