package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "printer")
@SQLRestriction("deleted_at IS NULL")
public class PrinterEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @Column(name = "store_id")
    public UUID storeId;
    public String name;
    @Column(name = "connection_type")
    public String connectionType;
    public String address;
    public boolean active;
    @Column(name = "paper_width_mm")
    public int paperWidthMm = 80;
    public int port = 9100;
    @Column(name = "code_page")
    public String codePage = "CP860";
    @Column(name = "cut_paper")
    public boolean cutPaper = true;
    @CreationTimestamp
    @Column(name = "created_at")
    public Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}
