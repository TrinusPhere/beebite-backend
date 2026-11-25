package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinusphere.beebite.application.sync.port.EdgeSnapshotApplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeSnapshotApplier implements EdgeSnapshotApplier {

    private static final Set<String> VALID_ORDER_TYPES = Set.of("BALCAO", "DELIVERY");
    private static final Set<String> VALID_ORDER_STATUSES = Set.of(
            "ANALISE", "PREPARO", "EM_ROTA", "ENTREGUE", "FINALIZADO", "CANCELADO");
    private static final Set<String> VALID_PAYMENT_METHODS = Set.of("PIX", "CREDITO", "DEBITO", "DINHEIRO");
    private static final Set<String> VALID_PLATFORMS = Set.of("OWN", "IFOOD", "WHATSAPP");

    private final EntityManager em;
    private final ObjectMapper objectMapper;

    public PanacheEdgeSnapshotApplier() {
        this(null, new ObjectMapper());
    }

    @Inject
    public PanacheEdgeSnapshotApplier(EntityManager em, ObjectMapper objectMapper) {
        this.em = em;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void apply(UUID storeId, String eventType, UUID aggregateId, String payloadJson) {
        if (storeId == null || eventType == null) {
            return;
        }

        JsonNode root = parseJsonStrict(payloadJson);

        switch (eventType) {
            case "ORDER_UPSERT" -> applyOrderUpsert(storeId, aggregateId, root);
            case "FINANCIAL_RECORD_UPSERT" -> applyFinancialRecordUpsert(storeId, aggregateId, root);
            case "CASH_REGISTER_UPSERT" -> applyCashRegisterUpsert(storeId, aggregateId, root);
            case "CASH_SESSION_UPSERT" -> applyCashSessionUpsert(storeId, aggregateId, root);
            case "CASH_MOVEMENT_UPSERT" -> applyCashMovementUpsert(storeId, aggregateId, root);
            case "PAYMENT_UPSERT" -> applyPaymentUpsert(storeId, aggregateId, root);
            case "STORE_STATUS_UPSERT" -> applyStoreStatusUpsert(storeId, root);
            default -> {
                // Eventos futuros desconhecidos são ignorados de forma segura
            }
        }
    }

    private JsonNode parseJsonStrict(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            throw new IllegalStateException("Payload JSON vazio no evento de sincronização Edge.");
        }
        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            if (root == null || !root.isObject()) {
                throw new IllegalStateException("Payload JSON inválido (esperado objeto JSON).");
            }
            return root;
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao interpretar payload JSON de sincronização Edge: " + ex.getMessage(), ex);
        }
    }

    private void applyOrderUpsert(UUID storeId, UUID aggregateId, JsonNode root) {
        if (aggregateId == null) {
            return;
        }

        String displayId = textOrDefault(root, "displayId", "#EDGE");
        String type = enumOrDefault(textOrNull(root, "type"), VALID_ORDER_TYPES, "BALCAO");
        String status = enumOrDefault(textOrNull(root, "status"), VALID_ORDER_STATUSES, "ANALISE");
        String paymentMethod = enumOrDefault(textOrNull(root, "paymentMethod"), VALID_PAYMENT_METHODS, "PIX");
        String platform = enumOrDefault(textOrNull(root, "platform"), VALID_PLATFORMS, "OWN");

        String defaultOp = "DELIVERY".equals(type) ? "DELIVERY" : "BALCAO";
        String operation = textOrDefault(root, "operation", defaultOp);
        String channel = textOrDefault(root, "channel", "PDV");
        String serviceType = textOrDefault(root, "serviceType", defaultOp);
        String source = textOrDefault(root, "source", "SISTEMA");

        BigDecimal totalValue = decimalOrNull(root, "totalValue");
        if (totalValue == null) {
            totalValue = decimalOrDefault(root, "totalAmount", BigDecimal.ZERO);
        }
        BigDecimal deliveryCost = decimalOrDefault(root, "deliveryCost", BigDecimal.ZERO);
        BigDecimal subtotal = decimalOrDefault(root, "subtotal", totalValue);
        BigDecimal cashChangeFor = decimalOrNull(root, "cashChangeFor");

        UUID rawCustomerId = uuidOrNull(root, "customerId");
        UUID resolvedCustomerId = (rawCustomerId != null && existsInTable("customer", rawCustomerId)) ? rawCustomerId : null;

        UUID rawDriverId = uuidOrNull(root, "driverId");
        UUID resolvedDriverId = (rawDriverId != null && existsInTable("delivery_driver", rawDriverId)) ? rawDriverId : null;

        String nowIso = LocalDateTime.now().toString();
        String createdAt = textOrDefault(root, "createdAt", nowIso);
        String updatedAt = textOrDefault(root, "updatedAt", nowIso);
        String finishedAt = textOrNull(root, "finishedAt");
        if ((finishedAt == null || finishedAt.isBlank()) && "FINALIZADO".equals(status)) {
            finishedAt = updatedAt;
        }

        boolean nfeEmitted = root.hasNonNull("nfeEmitted") && root.get("nfeEmitted").asBoolean(false);

        em.createNativeQuery("""
                INSERT INTO orders (
                    id, store_id, version, display_id,
                    customer_id, customer_name, customer_address, customer_phone,
                    customer_address_complement, customer_address_reference,
                    type, status, payment_method,
                    driver_id, driver_name, platform, external_order_id,
                    operation, channel, service_type, source,
                    cash_change_for, notes,
                    subtotal, delivery_cost, total_value, nfe_emitted,
                    created_at, updated_at, finished_at
                ) VALUES (
                    :id, :storeId, 0, :displayId,
                    NULLIF(:customerId, '')::uuid, NULLIF(:customerName, ''), NULLIF(:customerAddress, ''), NULLIF(:customerPhone, ''),
                    NULLIF(:customerAddressComplement, ''), NULLIF(:customerAddressReference, ''),
                    :type, :status, :paymentMethod,
                    NULLIF(:driverId, '')::uuid, NULLIF(:driverName, ''), :platform, NULLIF(:externalOrderId, ''),
                    :operation, :channel, :serviceType, :source,
                    NULLIF(:cashChangeFor, '')::numeric, NULLIF(:notes, ''),
                    :subtotal::numeric, :deliveryCost::numeric, :totalValue::numeric, :nfeEmitted,
                    :createdAt::timestamp, :updatedAt::timestamp, NULLIF(:finishedAt, '')::timestamp
                )
                ON CONFLICT (id) DO UPDATE SET
                    display_id = EXCLUDED.display_id,
                    customer_id = COALESCE(EXCLUDED.customer_id, orders.customer_id),
                    customer_name = COALESCE(EXCLUDED.customer_name, orders.customer_name),
                    customer_address = COALESCE(EXCLUDED.customer_address, orders.customer_address),
                    customer_phone = COALESCE(EXCLUDED.customer_phone, orders.customer_phone),
                    customer_address_complement = COALESCE(EXCLUDED.customer_address_complement, orders.customer_address_complement),
                    customer_address_reference = COALESCE(EXCLUDED.customer_address_reference, orders.customer_address_reference),
                    type = EXCLUDED.type,
                    status = EXCLUDED.status,
                    payment_method = EXCLUDED.payment_method,
                    driver_id = COALESCE(EXCLUDED.driver_id, orders.driver_id),
                    driver_name = COALESCE(EXCLUDED.driver_name, orders.driver_name),
                    platform = EXCLUDED.platform,
                    operation = EXCLUDED.operation,
                    channel = EXCLUDED.channel,
                    service_type = EXCLUDED.service_type,
                    source = EXCLUDED.source,
                    cash_change_for = EXCLUDED.cash_change_for,
                    notes = COALESCE(EXCLUDED.notes, orders.notes),
                    subtotal = EXCLUDED.subtotal,
                    delivery_cost = EXCLUDED.delivery_cost,
                    total_value = EXCLUDED.total_value,
                    nfe_emitted = EXCLUDED.nfe_emitted,
                    updated_at = EXCLUDED.updated_at,
                    finished_at = COALESCE(EXCLUDED.finished_at, orders.finished_at),
                    version = orders.version + 1
                """)
                .setParameter("id", aggregateId)
                .setParameter("storeId", storeId)
                .setParameter("displayId", displayId)
                .setParameter("customerId", uuidStr(resolvedCustomerId))
                .setParameter("customerName", emptyIfNull(textOrNull(root, "customerName")))
                .setParameter("customerAddress", emptyIfNull(textOrNull(root, "customerAddress")))
                .setParameter("customerPhone", emptyIfNull(textOrNull(root, "customerPhone")))
                .setParameter("customerAddressComplement", emptyIfNull(textOrNull(root, "customerAddressComplement")))
                .setParameter("customerAddressReference", emptyIfNull(textOrNull(root, "customerAddressReference")))
                .setParameter("type", type)
                .setParameter("status", status)
                .setParameter("paymentMethod", paymentMethod)
                .setParameter("driverId", uuidStr(resolvedDriverId))
                .setParameter("driverName", emptyIfNull(textOrNull(root, "driverName")))
                .setParameter("platform", platform)
                .setParameter("externalOrderId", emptyIfNull(textOrNull(root, "externalOrderId")))
                .setParameter("operation", operation)
                .setParameter("channel", channel)
                .setParameter("serviceType", serviceType)
                .setParameter("source", source)
                .setParameter("cashChangeFor", decStr(cashChangeFor))
                .setParameter("notes", emptyIfNull(textOrNull(root, "notes")))
                .setParameter("subtotal", subtotal.toPlainString())
                .setParameter("deliveryCost", deliveryCost.toPlainString())
                .setParameter("totalValue", totalValue.toPlainString())
                .setParameter("nfeEmitted", nfeEmitted)
                .setParameter("createdAt", createdAt)
                .setParameter("updatedAt", updatedAt)
                .setParameter("finishedAt", emptyIfNull(finishedAt))
                .executeUpdate();

        JsonNode itemsNode = root.get("items");
        if (itemsNode != null && itemsNode.isArray()) {
            em.createNativeQuery("DELETE FROM order_item WHERE order_id = :orderId")
                    .setParameter("orderId", aggregateId)
                    .executeUpdate();

            for (JsonNode item : itemsNode) {
                UUID itemId = uuidOrNull(item, "id");
                if (itemId == null) {
                    itemId = UUID.randomUUID();
                }
                UUID rawProductId = uuidOrNull(item, "productId");
                UUID resolvedProductId = (rawProductId != null && existsInTable("product", rawProductId)) ? rawProductId : null;
                String productName = textOrDefault(item, "productName", "Item PDV");
                int quantity = item.hasNonNull("quantity") ? Math.max(1, item.get("quantity").asInt(1)) : 1;
                BigDecimal unitPrice = decimalOrDefault(item, "unitPrice", BigDecimal.ZERO);
                BigDecimal extraPrice = decimalOrDefault(item, "extraPrice", BigDecimal.ZERO);
                BigDecimal totalPrice = decimalOrDefault(item, "totalPrice", unitPrice.multiply(BigDecimal.valueOf(quantity)));

                em.createNativeQuery("""
                        INSERT INTO order_item (
                            id, order_id, product_id, product_name,
                            quantity, unit_price, extra_price, total_price, observation
                        ) VALUES (
                            :id, :orderId, NULLIF(:productId, '')::uuid, :productName,
                            :quantity, :unitPrice::numeric, :extraPrice::numeric, :totalPrice::numeric, NULLIF(:observation, '')
                        )
                        ON CONFLICT (id) DO UPDATE SET
                            product_name = EXCLUDED.product_name,
                            quantity = EXCLUDED.quantity,
                            unit_price = EXCLUDED.unit_price,
                            extra_price = EXCLUDED.extra_price,
                            total_price = EXCLUDED.total_price,
                            observation = EXCLUDED.observation
                        """)
                        .setParameter("id", itemId)
                        .setParameter("orderId", aggregateId)
                        .setParameter("productId", uuidStr(resolvedProductId))
                        .setParameter("productName", productName)
                        .setParameter("quantity", quantity)
                        .setParameter("unitPrice", unitPrice.toPlainString())
                        .setParameter("extraPrice", extraPrice.toPlainString())
                        .setParameter("totalPrice", totalPrice.toPlainString())
                        .setParameter("observation", emptyIfNull(textOrNull(item, "observation")))
                        .executeUpdate();
            }
        }
    }

    private void applyFinancialRecordUpsert(UUID storeId, UUID aggregateId, JsonNode root) {
        if (aggregateId == null) {
            return;
        }
        UUID orderId = uuidOrNull(root, "orderId");
        if (orderId == null) {
            return;
        }

        BigDecimal totalValue = decimalOrDefault(root, "totalValue", BigDecimal.ZERO);
        String paymentMethod = enumOrDefault(textOrNull(root, "paymentMethod"), VALID_PAYMENT_METHODS, "PIX");
        String status = textOrDefault(root, "status", "CONCLUIDO");
        String recordDate = textOrDefault(root, "recordDate", LocalDate.now().toString());
        String recordTime = textOrDefault(root, "recordTime", LocalTime.now().withNano(0).toString());
        String createdAt = textOrDefault(root, "createdAt", LocalDateTime.now().toString());

        // Garante integridade referencial caso o registro financeiro chegue antes do pedido
        if (!existsInTable("orders", orderId)) {
            em.createNativeQuery("""
                    INSERT INTO orders (
                        id, store_id, version, display_id, type, status, payment_method, platform,
                        operation, channel, service_type, source,
                        subtotal, delivery_cost, total_value, nfe_emitted,
                        created_at, updated_at, finished_at
                    ) VALUES (
                        :orderId, :storeId, 0, '#EDGE', 'BALCAO', 'FINALIZADO', :paymentMethod, 'OWN',
                        'BALCAO', 'PDV', 'BALCAO', 'SISTEMA',
                        :totalValue::numeric, 0, :totalValue::numeric, false,
                        :createdAt::timestamp, :createdAt::timestamp, :createdAt::timestamp
                    )
                    ON CONFLICT (id) DO NOTHING
                    """)
                    .setParameter("orderId", orderId)
                    .setParameter("storeId", storeId)
                    .setParameter("paymentMethod", paymentMethod)
                    .setParameter("totalValue", totalValue.toPlainString())
                    .setParameter("createdAt", createdAt)
                    .executeUpdate();
        }

        em.createNativeQuery("""
                INSERT INTO financial_record (
                    id, store_id, order_id, record_date, record_time,
                    total_value, payment_method, status, created_at
                ) VALUES (
                    :id, :storeId, :orderId, :recordDate::date, :recordTime::time,
                    :totalValue::numeric, :paymentMethod, :status, :createdAt::timestamp
                )
                ON CONFLICT (id) DO UPDATE SET
                    record_date = EXCLUDED.record_date,
                    record_time = EXCLUDED.record_time,
                    total_value = EXCLUDED.total_value,
                    payment_method = EXCLUDED.payment_method,
                    status = EXCLUDED.status
                """)
                .setParameter("id", aggregateId)
                .setParameter("storeId", storeId)
                .setParameter("orderId", orderId)
                .setParameter("recordDate", recordDate)
                .setParameter("recordTime", recordTime)
                .setParameter("totalValue", totalValue.toPlainString())
                .setParameter("paymentMethod", paymentMethod)
                .setParameter("status", status)
                .setParameter("createdAt", createdAt)
                .executeUpdate();

        JsonNode productsNode = root.get("products");
        if (productsNode != null && productsNode.isArray()) {
            em.createNativeQuery("DELETE FROM financial_record_product WHERE financial_record_id = :recordId")
                    .setParameter("recordId", aggregateId)
                    .executeUpdate();

            for (JsonNode p : productsNode) {
                UUID itemId = uuidOrNull(p, "id");
                if (itemId == null) {
                    itemId = UUID.randomUUID();
                }
                UUID productId = uuidOrNull(p, "productId");
                String productName = textOrDefault(p, "productName", "Produto PDV");
                int quantity = p.hasNonNull("quantity") ? Math.max(1, p.get("quantity").asInt(1)) : 1;
                BigDecimal price = decimalOrDefault(p, "price", BigDecimal.ZERO);

                em.createNativeQuery("""
                        INSERT INTO financial_record_product (
                            id, financial_record_id, product_id, product_name, quantity, price
                        ) VALUES (
                            :id, :recordId, NULLIF(:productId, '')::uuid, :productName, :quantity, :price::numeric
                        )
                        ON CONFLICT (id) DO UPDATE SET
                            product_name = EXCLUDED.product_name,
                            quantity = EXCLUDED.quantity,
                            price = EXCLUDED.price
                        """)
                        .setParameter("id", itemId)
                        .setParameter("recordId", aggregateId)
                        .setParameter("productId", uuidStr(productId))
                        .setParameter("productName", productName)
                        .setParameter("quantity", quantity)
                        .setParameter("price", price.toPlainString())
                        .executeUpdate();
            }
        }
    }

    private void applyCashRegisterUpsert(UUID storeId, UUID aggregateId, JsonNode root) {
        if (aggregateId == null) {
            return;
        }
        String name = textOrDefault(root, "name", "Caixa Balcão");
        boolean active = !root.hasNonNull("active") || root.get("active").asBoolean(true);
        String nowIso = LocalDateTime.now().toString();
        String createdAt = textOrDefault(root, "createdAt", nowIso);
        String updatedAt = textOrDefault(root, "updatedAt", nowIso);
        String deletedAt = textOrNull(root, "deletedAt");

        em.createNativeQuery("""
                INSERT INTO cash_register (
                    id, store_id, name, active, created_at, updated_at, deleted_at
                ) VALUES (
                    :id, :storeId, :name, :active, :createdAt::timestamp, :updatedAt::timestamp, NULLIF(:deletedAt, '')::timestamp
                )
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    active = EXCLUDED.active,
                    updated_at = EXCLUDED.updated_at,
                    deleted_at = EXCLUDED.deleted_at
                """)
                .setParameter("id", aggregateId)
                .setParameter("storeId", storeId)
                .setParameter("name", name)
                .setParameter("active", active)
                .setParameter("createdAt", createdAt)
                .setParameter("updatedAt", updatedAt)
                .setParameter("deletedAt", emptyIfNull(deletedAt))
                .executeUpdate();
    }

    private void applyCashSessionUpsert(UUID storeId, UUID aggregateId, JsonNode root) {
        if (aggregateId == null) {
            return;
        }
        UUID cashRegisterId = uuidOrNull(root, "cashRegisterId");
        if (cashRegisterId == null) {
            return;
        }

        if (!existsInTable("cash_register", cashRegisterId)) {
            em.createNativeQuery("""
                    INSERT INTO cash_register (id, store_id, name, active, created_at, updated_at)
                    VALUES (:id, :storeId, 'Caixa Balcão', true, now(), now())
                    ON CONFLICT (id) DO NOTHING
                    """)
                    .setParameter("id", cashRegisterId)
                    .setParameter("storeId", storeId)
                    .executeUpdate();
        }

        UUID resolvedUserId = resolveCloudUserId(storeId, uuidOrNull(root, "userId"));
        if (resolvedUserId == null) {
            throw new IllegalStateException("Nenhum usuário válido encontrado na nuvem para vincular sessão de caixa da loja " + storeId);
        }

        String status = textOrDefault(root, "status", "OPEN");
        String nowIso = LocalDateTime.now().toString();
        String openedAt = textOrDefault(root, "openedAt", nowIso);
        String closedAt = textOrNull(root, "closedAt");
        String createdAt = textOrDefault(root, "createdAt", openedAt);
        String updatedAt = textOrDefault(root, "updatedAt", nowIso);

        BigDecimal initialAmount = decimalOrDefault(root, "initialAmount", BigDecimal.ZERO);
        BigDecimal expectedAmount = decimalOrNull(root, "expectedAmount");
        BigDecimal countedAmount = decimalOrNull(root, "countedAmount");
        BigDecimal differenceAmount = decimalOrNull(root, "differenceAmount");

        // Respeita o índice parcial único idx_unique_open_session_per_user (store_id, user_id) WHERE status = 'OPEN'
        if ("OPEN".equalsIgnoreCase(status)) {
            em.createNativeQuery("""
                    UPDATE cash_session
                    SET status = 'CLOSED',
                        closed_at = COALESCE(closed_at, now()),
                        updated_at = now()
                    WHERE store_id = :storeId
                      AND user_id = :userId
                      AND status = 'OPEN'
                      AND id <> :sessionId
                    """)
                    .setParameter("storeId", storeId)
                    .setParameter("userId", resolvedUserId)
                    .setParameter("sessionId", aggregateId)
                    .executeUpdate();
        }

        em.createNativeQuery("""
                INSERT INTO cash_session (
                    id, store_id, cash_register_id, user_id, status,
                    opened_at, closed_at, initial_amount,
                    expected_amount, counted_amount, difference_amount, closing_notes,
                    created_at, updated_at
                ) VALUES (
                    :id, :storeId, :cashRegisterId, :userId, :status,
                    :openedAt::timestamp, NULLIF(:closedAt, '')::timestamp, :initialAmount::numeric,
                    NULLIF(:expectedAmount, '')::numeric, NULLIF(:countedAmount, '')::numeric,
                    NULLIF(:differenceAmount, '')::numeric, NULLIF(:closingNotes, ''),
                    :createdAt::timestamp, :updatedAt::timestamp
                )
                ON CONFLICT (id) DO UPDATE SET
                    status = EXCLUDED.status,
                    closed_at = COALESCE(EXCLUDED.closed_at, cash_session.closed_at),
                    initial_amount = EXCLUDED.initial_amount,
                    expected_amount = COALESCE(EXCLUDED.expected_amount, cash_session.expected_amount),
                    counted_amount = COALESCE(EXCLUDED.counted_amount, cash_session.counted_amount),
                    difference_amount = COALESCE(EXCLUDED.difference_amount, cash_session.difference_amount),
                    closing_notes = COALESCE(EXCLUDED.closing_notes, cash_session.closing_notes),
                    updated_at = EXCLUDED.updated_at
                """)
                .setParameter("id", aggregateId)
                .setParameter("storeId", storeId)
                .setParameter("cashRegisterId", cashRegisterId)
                .setParameter("userId", resolvedUserId)
                .setParameter("status", status)
                .setParameter("openedAt", openedAt)
                .setParameter("closedAt", emptyIfNull(closedAt))
                .setParameter("initialAmount", initialAmount.toPlainString())
                .setParameter("expectedAmount", decStr(expectedAmount))
                .setParameter("countedAmount", decStr(countedAmount))
                .setParameter("differenceAmount", decStr(differenceAmount))
                .setParameter("closingNotes", emptyIfNull(textOrNull(root, "closingNotes")))
                .setParameter("createdAt", createdAt)
                .setParameter("updatedAt", updatedAt)
                .executeUpdate();
    }

    private void applyCashMovementUpsert(UUID storeId, UUID aggregateId, JsonNode root) {
        if (aggregateId == null) {
            return;
        }
        UUID cashSessionId = uuidOrNull(root, "cashSessionId");
        if (cashSessionId == null || !existsInTable("cash_session", cashSessionId)) {
            return;
        }
        UUID resolvedUserId = resolveCloudUserId(storeId, uuidOrNull(root, "userId"));
        if (resolvedUserId == null) {
            return;
        }

        String type = textOrDefault(root, "type", "SUPRIMENTO");
        BigDecimal amount = decimalOrDefault(root, "amount", BigDecimal.ZERO);
        String reason = textOrDefault(root, "reason", "Movimentação Edge");
        String createdAt = textOrDefault(root, "createdAt", LocalDateTime.now().toString());

        em.createNativeQuery("""
                INSERT INTO cash_movement (
                    id, store_id, cash_session_id, user_id, type, amount, reason, created_at
                ) VALUES (
                    :id, :storeId, :cashSessionId, :userId, :type, :amount::numeric, :reason, :createdAt::timestamp
                )
                ON CONFLICT (id) DO UPDATE SET
                    type = EXCLUDED.type,
                    amount = EXCLUDED.amount,
                    reason = EXCLUDED.reason
                """)
                .setParameter("id", aggregateId)
                .setParameter("storeId", storeId)
                .setParameter("cashSessionId", cashSessionId)
                .setParameter("userId", resolvedUserId)
                .setParameter("type", type)
                .setParameter("amount", amount.toPlainString())
                .setParameter("reason", reason)
                .setParameter("createdAt", createdAt)
                .executeUpdate();
    }

    private void applyPaymentUpsert(UUID storeId, UUID aggregateId, JsonNode root) {
        if (aggregateId == null) {
            return;
        }
        UUID rawOrderId = uuidOrNull(root, "orderId");
        UUID resolvedOrderId = (rawOrderId != null && existsInTable("orders", rawOrderId)) ? rawOrderId : null;

        UUID rawSessionId = uuidOrNull(root, "cashSessionId");
        UUID resolvedSessionId = (rawSessionId != null && existsInTable("cash_session", rawSessionId)) ? rawSessionId : null;

        UUID rawDriverId = uuidOrNull(root, "driverId");
        UUID resolvedDriverId = (rawDriverId != null && existsInTable("delivery_driver", rawDriverId)) ? rawDriverId : null;

        String method = textOrDefault(root, "method", "PIX");
        BigDecimal amount = decimalOrDefault(root, "amount", BigDecimal.ZERO);
        BigDecimal changeFor = decimalOrNull(root, "changeFor");
        String status = textOrDefault(root, "status", "CONFIRMED");
        String collectedBy = textOrDefault(root, "collectedBy", "CAIXA");
        String nowIso = LocalDateTime.now().toString();
        String paidAt = textOrDefault(root, "paidAt", nowIso);
        String createdAt = textOrDefault(root, "createdAt", paidAt);

        em.createNativeQuery("""
                INSERT INTO payment (
                    id, store_id, order_id, cash_session_id, driver_id,
                    method, amount, change_for, status, collected_by, notes, paid_at, created_at
                ) VALUES (
                    :id, :storeId, NULLIF(:orderId, '')::uuid, NULLIF(:cashSessionId, '')::uuid, NULLIF(:driverId, '')::uuid,
                    :method, :amount::numeric, NULLIF(:changeFor, '')::numeric, :status, :collectedBy, NULLIF(:notes, ''),
                    :paidAt::timestamp, :createdAt::timestamp
                )
                ON CONFLICT (id) DO UPDATE SET
                    order_id = COALESCE(EXCLUDED.order_id, payment.order_id),
                    cash_session_id = COALESCE(EXCLUDED.cash_session_id, payment.cash_session_id),
                    driver_id = COALESCE(EXCLUDED.driver_id, payment.driver_id),
                    method = EXCLUDED.method,
                    amount = EXCLUDED.amount,
                    change_for = EXCLUDED.change_for,
                    status = EXCLUDED.status,
                    collected_by = EXCLUDED.collected_by,
                    notes = COALESCE(EXCLUDED.notes, payment.notes),
                    paid_at = EXCLUDED.paid_at
                """)
                .setParameter("id", aggregateId)
                .setParameter("storeId", storeId)
                .setParameter("orderId", uuidStr(resolvedOrderId))
                .setParameter("cashSessionId", uuidStr(resolvedSessionId))
                .setParameter("driverId", uuidStr(resolvedDriverId))
                .setParameter("method", method)
                .setParameter("amount", amount.toPlainString())
                .setParameter("changeFor", decStr(changeFor))
                .setParameter("status", status)
                .setParameter("collectedBy", collectedBy)
                .setParameter("notes", emptyIfNull(textOrNull(root, "notes")))
                .setParameter("paidAt", paidAt)
                .setParameter("createdAt", createdAt)
                .executeUpdate();
    }

    private void applyStoreStatusUpsert(UUID storeId, JsonNode root) {
        boolean open = !root.hasNonNull("open") || root.get("open").asBoolean(true);
        String openingHours = textOrNull(root, "openingHours");

        em.createNativeQuery("""
                UPDATE store
                SET is_open = :open,
                    opening_hours = COALESCE(NULLIF(:openingHours, ''), opening_hours),
                    updated_at = now()
                WHERE id = :storeId
                """)
                .setParameter("open", open)
                .setParameter("openingHours", emptyIfNull(openingHours))
                .setParameter("storeId", storeId)
                .executeUpdate();
    }

    private boolean existsInTable(String tableName, UUID id) {
        if (id == null) {
            return false;
        }
        List<?> rows = em.createNativeQuery("SELECT 1 FROM " + tableName + " WHERE id = :id LIMIT 1")
                .setParameter("id", id)
                .getResultList();
        return rows != null && !rows.isEmpty();
    }

    private UUID resolveCloudUserId(UUID storeId, UUID rawUserId) {
        if (rawUserId != null && existsInTable("users", rawUserId)) {
            return rawUserId;
        }

        UUID fromAccess = querySingleUuid("""
                SELECT u.id
                FROM user_store_access usa
                JOIN users u ON u.id = usa.user_id
                WHERE usa.store_id = :storeId
                  AND u.deleted_at IS NULL
                ORDER BY CASE WHEN usa.role = 'ADMIN' THEN 0 ELSE 1 END, usa.created_at ASC
                LIMIT 1
                """, storeId);
        if (fromAccess != null) {
            return fromAccess;
        }

        UUID fromStore = querySingleUuid("""
                SELECT id FROM users
                WHERE store_id = :storeId AND deleted_at IS NULL
                ORDER BY created_at ASC
                LIMIT 1
                """, storeId);
        if (fromStore != null) {
            return fromStore;
        }

        List<?> fallback = em.createNativeQuery("""
                SELECT id FROM users
                WHERE deleted_at IS NULL
                ORDER BY CASE WHEN role = 'SUPER_ADMIN' THEN 0 ELSE 1 END, created_at ASC
                LIMIT 1
                """).getResultList();
        return extractFirstUuid(fallback);
    }

    private UUID querySingleUuid(String sql, UUID storeId) {
        List<?> rows = em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .getResultList();
        return extractFirstUuid(rows);
    }

    private UUID extractFirstUuid(List<?> rows) {
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            return null;
        }
        Object first = rows.get(0);
        if (first instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(first.toString());
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String text = node.get(field).asText();
        return text != null && !text.isBlank() ? text : null;
    }

    private String textOrDefault(JsonNode node, String field, String fallback) {
        String val = textOrNull(node, field);
        return val != null ? val : fallback;
    }

    private String enumOrDefault(String value, Set<String> allowed, String fallback) {
        if (value == null) {
            return fallback;
        }
        String upper = value.trim().toUpperCase();
        return allowed.contains(upper) ? upper : fallback;
    }

    private UUID uuidOrNull(JsonNode node, String field) {
        String raw = textOrNull(node, field);
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        String raw = textOrNull(node, field);
        if (raw == null) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal decimalOrDefault(JsonNode node, String field, BigDecimal fallback) {
        BigDecimal val = decimalOrNull(node, field);
        return val != null ? val : fallback;
    }

    private String emptyIfNull(String value) {
        return value != null ? value : "";
    }

    private String uuidStr(UUID id) {
        return id != null ? id.toString() : "";
    }

    private String decStr(BigDecimal val) {
        return val != null ? val.toPlainString() : "";
    }
}
