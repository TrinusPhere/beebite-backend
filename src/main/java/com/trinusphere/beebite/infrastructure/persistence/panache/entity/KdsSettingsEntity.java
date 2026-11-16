package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kds_settings")
public class KdsSettingsEntity {

    @Id
    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "dispatch_mode", nullable = false, length = 20)
    public String dispatchMode;

    @Column(name = "warning_alert_minutes", nullable = false)
    public int warningAlertMinutes;

    @Column(name = "critical_alert_minutes", nullable = false)
    public int criticalAlertMinutes;

    @Column(name = "auto_complete_on_expedition", nullable = false)
    public boolean autoCompleteOnExpedition;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;
}

