package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.infrastructure.persistence.panache.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * Repositório Panache interno para UserEntity. Não implementa a porta de domínio — apenas fornece acesso Panache a
 * UserEntity. Injetado em PanacheUserRepository para uso interno.
 */
@ApplicationScoped
public class UserPanacheRepo implements PanacheRepositoryBase<UserEntity, UUID> {
    // Herda todos os métodos Panache: findById, find, list, persist, deleteById, count, etc.
}
