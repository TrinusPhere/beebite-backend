package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CatalogEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CatalogPanacheRepo implements PanacheRepositoryBase<CatalogEntity, UUID> {
}

