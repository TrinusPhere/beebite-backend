package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "saas_contract")
public class SaasContractEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "plan_name", nullable = false, length = 80)
    public String planName;

    @Column(name = "billing_cycle", nullable = false, length = 20)
    public String billingCycle;

    @Column(name = "monthly_fee", nullable = false, precision = 12, scale = 2)
    public BigDecimal monthlyFee;

    @Column(name = "setup_fee", nullable = false, precision = 12, scale = 2)
    public BigDecimal setupFee;

    @Column(name = "max_stores", nullable = false)
    public int maxStores;

    @Column(name = "includes_ifood", nullable = false)
    public boolean includesIfood;

    @Column(name = "includes_edge_sync", nullable = false)
    public boolean includesEdgeSync;

    @Column(name = "billing_day", nullable = false)
    public int billingDay;

    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    @Column(name = "end_date")
    public LocalDate endDate;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "contract_notes", columnDefinition = "TEXT")
    public String contractNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}

