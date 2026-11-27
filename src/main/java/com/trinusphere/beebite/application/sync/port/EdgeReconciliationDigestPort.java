package com.trinusphere.beebite.application.sync.port;

import com.trinusphere.beebite.application.sync.dto.ReconciliationBucketDigest;
import com.trinusphere.beebite.application.sync.dto.ReconciliationItemFingerprint;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EdgeReconciliationDigestPort {
    List<ReconciliationBucketDigest> computeBucketDigests(UUID storeId, LocalDate from, LocalDate to);

    List<ReconciliationItemFingerprint> computeItemFingerprints(
            UUID storeId,
            ReconciliationEntityType type,
            LocalDate bucketDate
    );
}

