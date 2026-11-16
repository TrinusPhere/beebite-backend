package com.trinusphere.beebite.domain.kds.entity;

import com.trinusphere.beebite.domain.kds.valueobject.DispatchMode;
import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.time.LocalDateTime;
import java.util.UUID;

public class KdsSettings implements StoreGuard.StoreScoped {

    private final UUID storeId;
    private DispatchMode dispatchMode;
    private int warningAlertMinutes;
    private int criticalAlertMinutes;
    private boolean autoCompleteOnExpedition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KdsSettings(UUID storeId, DispatchMode dispatchMode, int warningAlertMinutes,
                       int criticalAlertMinutes, boolean autoCompleteOnExpedition) {
        if (storeId == null) throw new IllegalArgumentException("storeId não pode ser nulo");
        if (warningAlertMinutes <= 0) throw new IllegalArgumentException("warningAlertMinutes deve ser maior que zero");
        if (criticalAlertMinutes <= warningAlertMinutes) {
            throw new IllegalArgumentException("criticalAlertMinutes deve ser maior que warningAlertMinutes");
        }
        this.storeId = storeId;
        this.dispatchMode = dispatchMode != null ? dispatchMode : DispatchMode.AUTO;
        this.warningAlertMinutes = warningAlertMinutes;
        this.criticalAlertMinutes = criticalAlertMinutes;
        this.autoCompleteOnExpedition = autoCompleteOnExpedition;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static KdsSettings createDefault(UUID storeId) {
        return new KdsSettings(storeId, DispatchMode.AUTO, 10, 20, false);
    }

    public static KdsSettings reconstitute(UUID storeId, DispatchMode dispatchMode, int warningAlertMinutes,
                                          int criticalAlertMinutes, boolean autoCompleteOnExpedition,
                                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        KdsSettings settings = new KdsSettings(storeId, dispatchMode, warningAlertMinutes,
                criticalAlertMinutes, autoCompleteOnExpedition);
        settings.createdAt = createdAt;
        settings.updatedAt = updatedAt;
        return settings;
    }

    public void update(DispatchMode dispatchMode, int warningAlertMinutes,
                       int criticalAlertMinutes, boolean autoCompleteOnExpedition) {
        if (warningAlertMinutes <= 0) throw new IllegalArgumentException("warningAlertMinutes deve ser maior que zero");
        if (criticalAlertMinutes <= warningAlertMinutes) {
            throw new IllegalArgumentException("criticalAlertMinutes deve ser maior que warningAlertMinutes");
        }
        this.dispatchMode = dispatchMode != null ? dispatchMode : DispatchMode.AUTO;
        this.warningAlertMinutes = warningAlertMinutes;
        this.criticalAlertMinutes = criticalAlertMinutes;
        this.autoCompleteOnExpedition = autoCompleteOnExpedition;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public DispatchMode getDispatchMode() {
        return dispatchMode;
    }

    public int getWarningAlertMinutes() {
        return warningAlertMinutes;
    }

    public int getCriticalAlertMinutes() {
        return criticalAlertMinutes;
    }

    public boolean isAutoCompleteOnExpedition() {
        return autoCompleteOnExpedition;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

