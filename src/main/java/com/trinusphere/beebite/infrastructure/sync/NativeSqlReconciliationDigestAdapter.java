package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.dto.ReconciliationBucketDigest;
import com.trinusphere.beebite.application.sync.dto.ReconciliationItemFingerprint;
import com.trinusphere.beebite.application.sync.port.EdgeReconciliationDigestPort;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NativeSqlReconciliationDigestAdapter implements EdgeReconciliationDigestPort {

    private static final LocalDate REGISTER_SINGLE_BUCKET = LocalDate.of(1970, 1, 1);

    @Inject
    EntityManager em;

    @Override
    @Transactional
    public List<ReconciliationBucketDigest> computeBucketDigests(UUID storeId, LocalDate from, LocalDate to) {
        List<ReconciliationBucketDigest> all = new ArrayList<>();
        for (ReconciliationEntityType type : ReconciliationEntityType.values()) {
            all.addAll(queryBucketsForType(storeId, type, from, to));
        }
        return all;
    }

    @Override
    @Transactional
    public List<ReconciliationItemFingerprint> computeItemFingerprints(
            UUID storeId,
            ReconciliationEntityType type,
            LocalDate bucketDate) {
        TableSpec spec = specFor(type);
        String sql;
        List<Object[]> rows;

        if (type == ReconciliationEntityType.CASH_REGISTER) {
            sql = String.format("""
                    SELECT id, %s AS fp
                    FROM %s
                    WHERE store_id = :storeId
                    ORDER BY id
                    """, spec.fingerprintExpr(), spec.table());
            @SuppressWarnings("unchecked")
            List<Object[]> res = em.createNativeQuery(sql)
                    .setParameter("storeId", storeId)
                    .getResultList();
            rows = res;
        } else if (spec.isDateColumn()) {
            sql = String.format("""
                    SELECT id, %s AS fp
                    FROM %s
                    WHERE store_id = :storeId
                      AND %s = :bucketDate
                    ORDER BY id
                    """, spec.fingerprintExpr(), spec.table(), spec.dateColumn());
            @SuppressWarnings("unchecked")
            List<Object[]> res = em.createNativeQuery(sql)
                    .setParameter("storeId", storeId)
                    .setParameter("bucketDate", Date.valueOf(bucketDate))
                    .getResultList();
            rows = res;
        } else {
            Timestamp dayStart = Timestamp.from(bucketDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            Timestamp dayEnd = Timestamp.from(bucketDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
            sql = String.format("""
                    SELECT id, %s AS fp
                    FROM %s
                    WHERE store_id = :storeId
                      AND %s >= :dayStart
                      AND %s < :dayEnd
                    ORDER BY id
                    """, spec.fingerprintExpr(), spec.table(), spec.dateColumn(), spec.dateColumn());
            @SuppressWarnings("unchecked")
            List<Object[]> res = em.createNativeQuery(sql)
                    .setParameter("storeId", storeId)
                    .setParameter("dayStart", dayStart)
                    .setParameter("dayEnd", dayEnd)
                    .getResultList();
            rows = res;
        }

        List<ReconciliationItemFingerprint> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            UUID id = row[0] instanceof UUID u ? u : UUID.fromString(row[0].toString());
            String fp = row[1] != null ? row[1].toString() : "";
            result.add(new ReconciliationItemFingerprint(id, fp));
        }
        return result;
    }

    private List<ReconciliationBucketDigest> queryBucketsForType(
            UUID storeId,
            ReconciliationEntityType type,
            LocalDate from,
            LocalDate to) {
        TableSpec spec = specFor(type);
        List<Object[]> rows;

        if (type == ReconciliationEntityType.CASH_REGISTER) {
            String sql = String.format("""
                    SELECT DATE '1970-01-01' AS bucket_date,
                           count(*)          AS cnt,
                           md5(string_agg(fp, ',' ORDER BY id)) AS hash
                    FROM (
                        SELECT id, %s AS fp
                        FROM %s
                        WHERE store_id = :storeId
                    ) t
                    HAVING count(*) > 0
                    """, spec.fingerprintExpr(), spec.table());
            @SuppressWarnings("unchecked")
            List<Object[]> res = em.createNativeQuery(sql)
                    .setParameter("storeId", storeId)
                    .getResultList();
            rows = res;
        } else if (spec.isDateColumn()) {
            String sql = String.format("""
                    SELECT bucket_date,
                           count(*) AS cnt,
                           md5(string_agg(fp, ',' ORDER BY id)) AS hash
                    FROM (
                        SELECT id, %s AS bucket_date, %s AS fp
                        FROM %s
                        WHERE store_id = :storeId
                          AND %s >= :fromDate
                          AND %s <= :toDate
                    ) t
                    GROUP BY bucket_date
                    ORDER BY bucket_date
                    """, spec.dateColumn(), spec.fingerprintExpr(), spec.table(), spec.dateColumn(), spec.dateColumn());
            @SuppressWarnings("unchecked")
            List<Object[]> res = em.createNativeQuery(sql)
                    .setParameter("storeId", storeId)
                    .setParameter("fromDate", Date.valueOf(from))
                    .setParameter("toDate", Date.valueOf(to))
                    .getResultList();
            rows = res;
        } else {
            Timestamp fromTs = Timestamp.from(from.atStartOfDay().toInstant(ZoneOffset.UTC));
            Timestamp toExclusiveTs = Timestamp.from(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
            String sql = String.format("""
                    SELECT bucket_date,
                           count(*) AS cnt,
                           md5(string_agg(fp, ',' ORDER BY id)) AS hash
                    FROM (
                        SELECT id, (%s)::date AS bucket_date, %s AS fp
                        FROM %s
                        WHERE store_id = :storeId
                          AND %s >= :fromTs
                          AND %s < :toExclusiveTs
                    ) t
                    GROUP BY bucket_date
                    ORDER BY bucket_date
                    """, spec.dateColumn(), spec.fingerprintExpr(), spec.table(), spec.dateColumn(), spec.dateColumn());
            @SuppressWarnings("unchecked")
            List<Object[]> res = em.createNativeQuery(sql)
                    .setParameter("storeId", storeId)
                    .setParameter("fromTs", fromTs)
                    .setParameter("toExclusiveTs", toExclusiveTs)
                    .getResultList();
            rows = res;
        }

        List<ReconciliationBucketDigest> out = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            LocalDate bucketDate = toLocalDate(row[0]);
            long cnt = ((Number) row[1]).longValue();
            String hash = row[2] != null ? row[2].toString() : "";
            out.add(new ReconciliationBucketDigest(type.name(), bucketDate, cnt, hash));
        }
        return out;
    }

    private LocalDate toLocalDate(Object raw) {
        if (raw == null) {
            return REGISTER_SINGLE_BUCKET;
        }
        if (raw instanceof LocalDate ld) {
            return ld;
        }
        if (raw instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(raw.toString());
    }

    private TableSpec specFor(ReconciliationEntityType type) {
        return switch (type) {
            case ORDER -> new TableSpec(
                    "orders",
                    "created_at",
                    false,
                    """
                    md5(concat_ws('|',
                        id::text,
                        coalesce(status, '∅'),
                        coalesce(type, '∅'),
                        coalesce(payment_method, '∅'),
                        coalesce(subtotal::numeric(14,2)::text, '∅'),
                        coalesce(delivery_cost::numeric(14,2)::text, '∅'),
                        coalesce(total_value::numeric(14,2)::text, '∅'),
                        coalesce(driver_id::text, '∅'),
                        coalesce(date_trunc('milliseconds', finished_at)::text, '∅')
                    ))"""
            );
            case PAYMENT -> new TableSpec(
                    "payment",
                    "created_at",
                    false,
                    """
                    md5(concat_ws('|',
                        id::text,
                        coalesce(order_id::text, '∅'),
                        coalesce(cash_session_id::text, '∅'),
                        coalesce(method, '∅'),
                        coalesce(amount::numeric(14,2)::text, '∅'),
                        coalesce(status, '∅')
                    ))"""
            );
            case FINANCIAL_RECORD -> new TableSpec(
                    "financial_record",
                    "record_date",
                    true,
                    """
                    md5(concat_ws('|',
                        id::text,
                        coalesce(order_id::text, '∅'),
                        coalesce(total_value::numeric(14,2)::text, '∅'),
                        coalesce(payment_method, '∅'),
                        coalesce(status, '∅')
                    ))"""
            );
            case CASH_SESSION -> new TableSpec(
                    "cash_session",
                    "opened_at",
                    false,
                    """
                    md5(concat_ws('|',
                        id::text,
                        coalesce(cash_register_id::text, '∅'),
                        coalesce(status, '∅'),
                        coalesce(initial_amount::numeric(14,2)::text, '∅'),
                        coalesce(expected_amount::numeric(14,2)::text, '∅'),
                        coalesce(counted_amount::numeric(14,2)::text, '∅'),
                        coalesce(difference_amount::numeric(14,2)::text, '∅'),
                        coalesce(date_trunc('milliseconds', closed_at)::text, '∅')
                    ))"""
            );
            case CASH_MOVEMENT -> new TableSpec(
                    "cash_movement",
                    "created_at",
                    false,
                    """
                    md5(concat_ws('|',
                        id::text,
                        coalesce(cash_session_id::text, '∅'),
                        coalesce(type, '∅'),
                        coalesce(amount::numeric(14,2)::text, '∅')
                    ))"""
            );
            case CASH_REGISTER -> new TableSpec(
                    "cash_register",
                    null,
                    false,
                    """
                    md5(concat_ws('|',
                        id::text,
                        coalesce(name, '∅'),
                        coalesce(active::text, '∅'),
                        (deleted_at IS NOT NULL)::text
                    ))"""
            );
        };
    }

    private record TableSpec(String table, String dateColumn, boolean isDateColumn, String fingerprintExpr) {
    }
}
