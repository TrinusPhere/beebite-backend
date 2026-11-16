package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinusphere.beebite.application.sync.port.EdgeSnapshotApplier;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.OrderEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.repository.OrderPanacheRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeSnapshotApplier implements EdgeSnapshotApplier {

    private static final Logger LOG = Logger.getLogger(PanacheEdgeSnapshotApplier.class);

    @Inject
    OrderPanacheRepo orderRepo;
    @Inject
    ObjectMapper objectMapper;

    @Override
    @Transactional
    public void apply(UUID storeId, String eventType, UUID aggregateId, String payloadJson) {
        if ("ORDER_UPSERT".equals(eventType)) {
            applyOrderSnapshot(storeId, aggregateId, payloadJson);
        } else {
            LOG.infof("Evento Edge recebido e registrado [store=%s, type=%s, aggregate=%s]",
                    storeId, eventType, aggregateId);
        }
    }

    private void applyOrderSnapshot(UUID storeId, UUID orderId, String payloadJson) {
        try {
            JsonNode node = objectMapper.readTree(payloadJson);
            OrderEntity entity = orderRepo.findByIdOptional(orderId).orElseGet(OrderEntity::new);
            boolean isNew = (entity.id == null);

            entity.id = orderId;
            entity.storeId = storeId;
            entity.displayId = textOrDefault(node, "displayId", "0");
            entity.customerName = textOrNull(node, "customerName");
            entity.customerAddress = textOrNull(node, "customerAddress");
            entity.customerPhone = textOrNull(node, "customerPhone");
            entity.type = textOrDefault(node, "type", "BALCAO");
            entity.status = textOrDefault(node, "status", "FINALIZADO");
            entity.paymentMethod = textOrDefault(node, "paymentMethod", "DINHEIRO");
            entity.platform = textOrDefault(node, "platform", "LOCAL");
            entity.operation = textOrDefault(node, "operation", "BALCAO");
            entity.channel = textOrDefault(node, "channel", "PDV");
            entity.serviceType = textOrDefault(node, "serviceType", "COUNTER");
            entity.source = textOrDefault(node, "source", "EDGE_PDV");
            entity.notes = textOrNull(node, "notes");
            entity.subtotal = decimalOrDefault(node, "subtotal", BigDecimal.ZERO);
            entity.deliveryCost = decimalOrDefault(node, "deliveryCost", BigDecimal.ZERO);
            entity.totalValue = decimalOrDefault(node, "totalAmount", entity.subtotal);
            if (entity.createdAt == null) {
                entity.createdAt = LocalDateTime.now();
            }
            entity.updatedAt = LocalDateTime.now();

            if (isNew) {
                orderRepo.getEntityManager().merge(entity);
            } else {
                orderRepo.getEntityManager().merge(entity);
            }
        } catch (Exception ex) {
            LOG.warnf("Falha ao aplicar snapshot ORDER_UPSERT [orderId=%s]: %s", orderId, ex.getMessage());
        }
    }

    private String textOrDefault(JsonNode node, String field, String fallback) {
        JsonNode val = node.get(field);
        return (val != null && !val.isNull() && !val.asText().isBlank()) ? val.asText() : fallback;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return (val != null && !val.isNull()) ? val.asText() : null;
    }

    private BigDecimal decimalOrDefault(JsonNode node, String field, BigDecimal fallback) {
        JsonNode val = node.get(field);
        if (val == null || val.isNull()) {
            return fallback;
        }
        try {
            return new BigDecimal(val.asText());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
