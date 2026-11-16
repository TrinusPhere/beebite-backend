package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.infrastructure.persistence.panache.entity.OrderEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderPanacheRepo implements PanacheRepositoryBase<OrderEntity, UUID> {

    /** Busca pedido pelo ID externo (ex: iFood). */
    public Optional<OrderEntity> findByExternalOrderId(String externalOrderId) {
        return find("externalOrderId", externalOrderId).firstResultOptional();
    }
}
