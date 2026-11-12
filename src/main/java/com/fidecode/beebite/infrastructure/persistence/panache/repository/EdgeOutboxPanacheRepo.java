package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.infrastructure.persistence.panache.entity.EdgeOutboxEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class EdgeOutboxPanacheRepo implements PanacheRepositoryBase<EdgeOutboxEntity, UUID> {
}
