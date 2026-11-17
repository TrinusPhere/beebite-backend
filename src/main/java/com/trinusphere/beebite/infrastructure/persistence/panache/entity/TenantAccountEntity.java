package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_account")
public class TenantAccountEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "company_name", nullable = false, length = 150)
    public String companyName;

    @Column(name = "trade_name", nullable = false, length = 150)
    public String tradeName;

    @Column(name = "owner_name", nullable = false, length = 120)
    public String ownerName;

    @Column(name = "document_number", length = 20)
    public String documentNumber;

    @Column(name = "email", length = 150)
    public String email;

    @Column(name = "phone", length = 30)
    public String phone;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}

