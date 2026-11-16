package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "phone")
    public String phone;

    @Column(name = "email")
    public String email;

    @Column(name = "zip_code")
    public String zipCode;

    @Column(name = "address_street")
    public String addressStreet;

    @Column(name = "address_number")
    public String addressNumber;

    @Column(name = "address_complement")
    public String addressComplement;

    @Column(name = "address_neighborhood")
    public String addressNeighborhood;

    @Column(name = "total_orders", nullable = false)
    public int totalOrders;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
