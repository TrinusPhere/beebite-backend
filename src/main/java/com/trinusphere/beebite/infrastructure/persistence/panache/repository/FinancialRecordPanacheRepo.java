package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.infrastructure.persistence.panache.entity.FinancialRecordEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * Panache repository helper — acesso direto ao Panache. Classe auxiliar que NÃO implementa o port de domínio.
 */
@ApplicationScoped
public class FinancialRecordPanacheRepo implements PanacheRepositoryBase<FinancialRecordEntity, UUID> {
}
