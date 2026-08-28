package com.fidecode.biteflow.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.*;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Subscriber do tópico: ifood-order-placed. Recebe novos pedidos do iFood e cria no banco do BiteFlow com status
 * ANALISE.
 *
 * Nota: Pedidos iFood chegam sem referência ao catálogo interno. Os itens são criados com nome/preço snapshot do evento
 * (ex: "X-Burger", R$ 25,90). A loja pode opcionalmente mapear depois.
 */
@ApplicationScoped
public class IfoodOrderSubscriber {

    private static final Logger LOG = Logger.getLogger(IfoodOrderSubscriber.class);
    private static final String PROJECT = "biteflow-dev";
    private static final String SUBSCRIPTION_PLACED = "ifood-order-placed-sub";
    private static final String SUBSCRIPTION_STATUS = "ifood-order-status-changed-sub";

    @ConfigProperty(name = "quarkus.google.cloud.pubsub.endpoint")
    Optional<String> emulatorEndpoint;

    @ConfigProperty(name = "quarkus.google.cloud.pubsub.use-plaintext")
    Optional<Boolean> usePlaintext;

    @Inject
    OrderRepository orderRepository;

    @Inject
    ObjectMapper objectMapper;

    private Subscriber placedSubscriber;
    private Subscriber statusSubscriber;
    private ManagedChannel channel;

    void onStart(@Observes StartupEvent event) {
        if (emulatorEndpoint.isEmpty() && !Boolean.TRUE.equals(usePlaintext.orElse(false))) {
            LOG.info("[IFOOD-SUB] Pub/Sub não configurado — subscriber iFood não iniciado.");
            return;
        }

        try {
            if (Boolean.TRUE.equals(usePlaintext.orElse(false)) && emulatorEndpoint.isPresent()) {
                channel = ManagedChannelBuilder.forTarget(emulatorEndpoint.get()).usePlaintext().build();
            }

            placedSubscriber = buildSubscriber(SUBSCRIPTION_PLACED, this::handleOrderPlaced);
            statusSubscriber = buildSubscriber(SUBSCRIPTION_STATUS, this::handleStatusChanged);

            placedSubscriber.startAsync().awaitRunning();
            statusSubscriber.startAsync().awaitRunning();

            LOG.info("[IFOOD-SUB] Subscribers iFood iniciados.");
        } catch (Exception e) {
            LOG.errorf(e, "[IFOOD-SUB] Falha ao iniciar subscribers: %s", e.getMessage());
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        stopSubscriber(placedSubscriber);
        stopSubscriber(statusSubscriber);
        if (channel != null)
            channel.shutdownNow();
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleOrderPlaced(PubsubMessage message, AckReplyConsumer consumer) {
        String payload = message.getData().toStringUtf8();
        try {
            createIfoodOrder(payload);
            consumer.ack();
            LOG.infof("[IFOOD-SUB] Pedido iFood criado com sucesso.");
        } catch (Exception e) {
            LOG.errorf(e, "[IFOOD-SUB] Erro ao criar pedido iFood — NACK: %s", e.getMessage());
            consumer.nack();
        }
    }

    private void handleStatusChanged(PubsubMessage message, AckReplyConsumer consumer) {
        String payload = message.getData().toStringUtf8();
        try {
            updateIfoodOrderStatus(payload);
            consumer.ack();
        } catch (Exception e) {
            LOG.errorf(e, "[IFOOD-SUB] Erro ao atualizar status iFood — NACK: %s", e.getMessage());
            consumer.nack();
        }
    }

    // ── Criação de pedido iFood ────────────────────────────────────────────────

    @Transactional
    void createIfoodOrder(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);

        UUID storeId = UUID.fromString(root.get("storeId").asText());
        String ifoodOrderId = root.get("ifoodOrderId").asText();
        String displayId = root.get("displayId").asText();
        String orderType = root.get("orderType").asText();

        // Verificar duplicata
        if (orderRepository.findByExternalOrderId(ifoodOrderId).isPresent()) {
            LOG.infof("[IFOOD-SUB] Pedido iFood já existe: %s — ignorando.", ifoodOrderId);
            return;
        }

        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setDisplayId(displayId);
        order.setPlatform(OrderPlatform.IFOOD);
        order.setExternalOrderId(ifoodOrderId);

        // Customer
        if (root.has("customerName") && !root.get("customerName").isNull()) {
            order.updateCustomerInfo(root.get("customerName").asText(), buildAddress(root));
        }

        // Items
        if (root.has("items")) {
            for (JsonNode item : root.get("items")) {
                OrderItem orderItem = new OrderItem(null, // productId null — pedido externo, sem mapeamento de catálogo
                        item.get("name").asText(), item.get("quantity").asInt(),
                        new BigDecimal(item.get("unitPrice").asText()), null // observation — não disponível em pedidos
                                                                             // iFood externos
                );
                order.addItem(orderItem);
            }
        }

        // Delivery cost
        if (root.has("deliveryFee") && !root.get("deliveryFee").isNull()) {
            order.setDeliveryCost(new BigDecimal(root.get("deliveryFee").asText()));
        }

        orderRepository.save(order);
        LOG.infof("[IFOOD-SUB] Pedido iFood criado: displayId=%s storeId=%s", displayId, storeId);
    }

    @Transactional
    void updateIfoodOrderStatus(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        String ifoodOrderId = root.get("ifoodOrderId").asText();
        String newStatus = root.get("newBiteflowStatus").asText();

        orderRepository.findByExternalOrderId(ifoodOrderId).ifPresentOrElse(order -> {
            order.changeStatus(OrderStatus.valueOf(newStatus));
            orderRepository.save(order);
            LOG.infof("[IFOOD-SUB] Status atualizado: %s → %s", ifoodOrderId, newStatus);
        }, () -> LOG.warnf("[IFOOD-SUB] Pedido não encontrado para atualizar status: %s", ifoodOrderId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildAddress(JsonNode root) {
        if (!root.has("deliveryAddress") || root.get("deliveryAddress").isNull())
            return null;
        JsonNode addr = root.get("deliveryAddress");
        StringBuilder sb = new StringBuilder();
        if (addr.has("streetName"))
            sb.append(addr.get("streetName").asText());
        if (addr.has("streetNumber"))
            sb.append(", ").append(addr.get("streetNumber").asText());
        if (addr.has("neighborhood"))
            sb.append(" - ").append(addr.get("neighborhood").asText());
        if (addr.has("city"))
            sb.append(", ").append(addr.get("city").asText());
        return sb.toString();
    }

    private Subscriber buildSubscriber(String subscriptionId, MessageReceiver receiver) {
        ProjectSubscriptionName subName = ProjectSubscriptionName.of(PROJECT, subscriptionId);

        if (channel != null) {
            var channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            return Subscriber.newBuilder(subName, receiver).setChannelProvider(channelProvider)
                    .setCredentialsProvider(NoCredentialsProvider.create()).build();
        }

        return Subscriber.newBuilder(subName, receiver).build();
    }

    private void stopSubscriber(Subscriber sub) {
        if (sub != null) {
            sub.stopAsync();
            try {
                sub.awaitTerminated(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }
    }
}
