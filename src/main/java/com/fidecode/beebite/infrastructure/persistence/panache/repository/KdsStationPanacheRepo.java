package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.infrastructure.persistence.panache.entity.KdsStationEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class KdsStationPanacheRepo implements PanacheRepositoryBase<KdsStationEntity, UUID> {
}
