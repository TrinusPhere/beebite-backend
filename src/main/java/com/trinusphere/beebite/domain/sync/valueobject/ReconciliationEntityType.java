package com.trinusphere.beebite.domain.sync.valueobject;

public enum ReconciliationEntityType {
    ORDER("ORDER_UPSERT"),
    PAYMENT("PAYMENT_UPSERT"),
    FINANCIAL_RECORD("FINANCIAL_RECORD_UPSERT"),
    CASH_SESSION("CASH_SESSION_UPSERT"),
    CASH_MOVEMENT("CASH_MOVEMENT_UPSERT"),
    CASH_REGISTER("CASH_REGISTER_UPSERT");

    private final String eventType;

    ReconciliationEntityType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }
}

