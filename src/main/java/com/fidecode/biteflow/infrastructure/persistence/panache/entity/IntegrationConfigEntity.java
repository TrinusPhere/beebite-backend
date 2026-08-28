package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integration_config")
public class IntegrationConfigEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @Column(name = "store_id")
    public UUID storeId;
    public String provider;
    public String status;
    @Column(name = "external_store_id")
    public String externalStoreId;
    @Column(name = "client_id")
    public String clientId;
    @Column(name = "client_secret")
    public String clientSecret;
    @Column(name = "access_token")
    public String accessToken;
    @Column(name = "business_account_id")
    public String businessAccountId;
    @Column(name = "phone_number")
    public String phoneNumber;
    @Column(name = "api_provider")
    public String apiProvider;
    @Column(name = "store_status")
    public String storeStatus;
    @CreationTimestamp
    @Column(name = "created_at")
    public Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
}
