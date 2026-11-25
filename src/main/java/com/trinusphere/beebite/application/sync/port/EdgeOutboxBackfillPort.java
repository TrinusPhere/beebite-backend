package com.trinusphere.beebite.application.sync.port;

import java.util.UUID;

public interface EdgeOutboxBackfillPort {

    int backfillMissingEvents(UUID storeId);
}
