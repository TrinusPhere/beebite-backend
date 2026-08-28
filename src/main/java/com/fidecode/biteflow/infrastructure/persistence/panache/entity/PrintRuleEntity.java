package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "print_rule")
@SQLRestriction("deleted_at IS NULL")
public class PrintRuleEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @Column(name = "store_id")
    public UUID storeId;
    public String name;
    public String operation;
    @Column(name = "document_type")
    public String documentType;
    @Column(name = "sector_id")
    public UUID sectorId;
    @Column(name = "printer_id")
    public UUID printerId;
    public boolean active;
    @CreationTimestamp
    @Column(name = "created_at")
    public Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}
