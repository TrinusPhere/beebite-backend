package com.trinusphere.beebite.application.sync.port;

import java.util.UUID;

public record EdgeEventOutcome(UUID eventId, Status status, String reason) {

    public enum Status {
        APPLIED,
        DUPLICATE,
        REJECTED
    }

    public static EdgeEventOutcome applied(UUID id) {
        return new EdgeEventOutcome(id, Status.APPLIED, null);
    }

    public static EdgeEventOutcome duplicate(UUID id) {
        return new EdgeEventOutcome(id, Status.DUPLICATE, null);
    }

    public static EdgeEventOutcome rejected(UUID id, String reason) {
        return new EdgeEventOutcome(id, Status.REJECTED, reason);
    }
}

