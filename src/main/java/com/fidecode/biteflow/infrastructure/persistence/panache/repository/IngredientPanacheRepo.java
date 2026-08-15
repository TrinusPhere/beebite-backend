package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.infrastructure.persistence.panache.entity.IngredientEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class IngredientPanacheRepo implements PanacheRepositoryBase<IngredientEntity, UUID> {
}
