package com.trinusphere.beebite.domain.platform.entity;

import com.trinusphere.beebite.domain.platform.valueobject.TenantStatus;

import java.time.Instant;
import java.util.UUID;

public class TenantAccount {
    private UUID id;
    private String companyName;
    private String tradeName;
    private String ownerName;
    private String documentNumber;
    private String email;
    private String phone;
    private TenantStatus status;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;

    public TenantAccount(
            String companyName,
            String tradeName,
            String ownerName,
            String documentNumber,
            String email,
            String phone,
            String notes) {
        validate(companyName, tradeName, ownerName);
        this.companyName = companyName.trim();
        this.tradeName = tradeName.trim();
        this.ownerName = ownerName.trim();
        this.documentNumber = documentNumber;
        this.email = email;
        this.phone = phone;
        this.status = TenantStatus.ACTIVE;
        this.notes = notes;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private TenantAccount(
            UUID id,
            String companyName,
            String tradeName,
            String ownerName,
            String documentNumber,
            String email,
            String phone,
            TenantStatus status,
            String notes,
            Instant createdAt,
            Instant updatedAt) {
        validate(companyName, tradeName, ownerName);
        this.id = id;
        this.companyName = companyName.trim();
        this.tradeName = tradeName.trim();
        this.ownerName = ownerName.trim();
        this.documentNumber = documentNumber;
        this.email = email;
        this.phone = phone;
        this.status = status != null ? status : TenantStatus.ACTIVE;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static TenantAccount reconstitute(
            UUID id,
            String companyName,
            String tradeName,
            String ownerName,
            String documentNumber,
            String email,
            String phone,
            TenantStatus status,
            String notes,
            Instant createdAt,
            Instant updatedAt) {
        return new TenantAccount(
                id, companyName, tradeName, ownerName, documentNumber, email, phone, status, notes, createdAt, updatedAt
        );
    }

    private static void validate(String companyName, String tradeName, String ownerName) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("A razão social / nome da empresa é obrigatória.");
        }
        if (tradeName == null || tradeName.isBlank()) {
            throw new IllegalArgumentException("O nome fantasia do cliente é obrigatório.");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("O nome do proprietário é obrigatório.");
        }
    }

    public void updateInfo(
            String companyName,
            String tradeName,
            String ownerName,
            String documentNumber,
            String email,
            String phone,
            String notes) {
        validate(companyName, tradeName, ownerName);
        this.companyName = companyName.trim();
        this.tradeName = tradeName.trim();
        this.ownerName = ownerName.trim();
        this.documentNumber = documentNumber;
        this.email = email;
        this.phone = phone;
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public void changeStatus(TenantStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status do cliente é obrigatório.");
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void suspend() {
        changeStatus(TenantStatus.SUSPENDED);
    }

    public void activate() {
        changeStatus(TenantStatus.ACTIVE);
    }

    public void cancel() {
        changeStatus(TenantStatus.CANCELLED);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

