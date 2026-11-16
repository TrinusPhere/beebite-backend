package com.trinusphere.beebite.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.entity.OrderItem;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.order.valueobject.*;
import com.trinusphere.beebite.domain.messaging.entity.Conversation;
import com.trinusphere.beebite.domain.messaging.entity.ConversationMessage;
import com.trinusphere.beebite.domain.messaging.repository.ConversationRepository;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;

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
 * Subscriber do tópico: ifood-order-placed. Recebe novos pedidos do iFood e
 * cria no banco do BiteFlow com status
 * ANALISE.
 *
 * Nota: Pedidos iFood chegam sem referência ao catálogo interno. Os itens são
 * criados com nome/preço snapshot do evento
 * (ex: "X-Burger", R$ 25,90). A loja pode opcionalmente mapear depois.
 */
@ApplicationScoped
public class IfoodOrderSubscriber {

    private static final Logger LOG = Logger.getLogger(IfoodOrderSubscriber.class);
    private static final String TOPIC_PLACED = "ifood-order-placed";
    private static final String TOPIC_STATUS = "ifood-order-status-changed";
    private static final String SUBSCRIPTION_PLACED = "ifood-order-placed-sub";
    private static final String SUBSCRIPTION_STATUS = "ifood-order-status-changed-sub";
    private static final String SUBSCRIPTION_CHAT = "ifood-chat-message-sub";

    @ConfigProperty(name = "quarkus.google.cloud.project-id", defaultValue = "beebite-dev")
    String projectId;

    @ConfigProperty(name = "biteflow.feature.ifood-integration.enabled", defaultValue = "true")
    boolean ifoodIntegrationEnabled;

    @ConfigProperty(name = "quarkus.google.cloud.pubsub.endpoint")
    Optional<String> emulatorEndpoint;

    @ConfigProperty(name = "quarkus.google.cloud.pubsub.use-plaintext")
    Optional<Boolean> usePlaintext;

    @Inject
    OrderRepository orderRepository;

    @Inject
    com.trinusphere.beebite.application.order.UpdateIfoodOrderStatusUseCase updateIfoodOrderStatusUseCase;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ObjectMapper objectMapper;

    private Subscriber placedSubscriber;
    private Subscriber statusSubscriber;
    private Subscriber chatSubscriber;
    private ManagedChannel channel;

    void onStart(@Observes StartupEvent event) {
        if (!ifoodIntegrationEnabled) {
            LOG.info("[IFOOD-SUB] Integração iFood desabilitada — subscriber iFood não iniciado.");
            return;
        }

        try {
            if (Boolean.TRUE.equals(usePlaintext.orElse(false)) && emulatorEndpoint.isPresent()) {
                channel = ManagedChannelBuilder.forTarget(emulatorEndpoint.get()).usePlaintext().build();
                ensureSubscriptionExists(SUBSCRIPTION_PLACED, TOPIC_PLACED);
                ensureSubscriptionExists(SUBSCRIPTION_STATUS, TOPIC_STATUS);
            }

            placedSubscriber = buildSubscriber(SUBSCRIPTION_PLACED, this::handleOrderPlaced);
            statusSubscriber = buildSubscriber(SUBSCRIPTION_STATUS, this::handleStatusChanged);
            chatSubscriber = buildSubscriber(SUBSCRIPTION_CHAT, this::handleChatMessage);

            placedSubscriber.startAsync().awaitRunning();
            statusSubscriber.startAsync().awaitRunning();
            chatSubscriber.startAsync().awaitRunning();

            LOG.infof("[IFOOD-SUB] Subscribers iFood iniciados (project=%s).", projectId);
        } catch (Exception e) {
            LOG.errorf(e, "[IFOOD-SUB] Falha ao iniciar subscribers: %s", e.getMessage());
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        stopSubscriber(placedSubscriber);
        stopSubscriber(statusSubscriber);
        stopSubscriber(chatSubscriber);
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

    private void handleChatMessage(PubsubMessage message, AckReplyConsumer consumer) {
        String payload = message.getData().toStringUtf8();
        try {
            saveIfoodChatMessage(payload);
            consumer.ack();
        } catch (Exception e) {
            LOG.errorf(e, "[IFOOD-SUB] Erro ao salvar chat iFood — NACK: %s", e.getMessage());
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

        OrderType resolvedType = "TAKEOUT".equalsIgnoreCase(orderType) ? OrderType.BALCAO : OrderType.DELIVERY;
        Order order = new Order(storeId, resolvedType, PaymentMethod.PIX);
        order.setDisplayId(displayId);
        order.setPlatform(OrderPlatform.IFOOD);
        order.setExternalOrderId(ifoodOrderId);
        String serviceType = resolvedType == OrderType.BALCAO ? "BALCAO" : "DELIVERY";
        order.defineContext(serviceType, "IFOOD", serviceType, "IFOOD_INTEGRATION");

        // Customer
        if (root.has("customerName") && !root.get("customerName").isNull()) {
            order.updateCustomerInfo(root.get("customerName").asText(), buildAddress(root));
        }
        if (root.has("customerPhone") && !root.get("customerPhone").isNull()
                && !root.get("customerPhone").asText().isBlank()) {
            order.setCustomerPhone(root.get("customerPhone").asText());
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

    void updateIfoodOrderStatus(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        String ifoodOrderId = root.get("ifoodOrderId").asText();
        String newStatus = root.get("newBiteflowStatus").asText();

        updateIfoodOrderStatusUseCase.execute(ifoodOrderId, OrderStatus.valueOf(newStatus));
    }

    @Transactional
    void saveIfoodChatMessage(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        UUID storeId = UUID.fromString(root.get("storeId").asText());
        String ifoodOrderId = root.get("orderId").asText();
        String content = root.get("content").asText();
        String sender = root.get("sender").asText();

        orderRepository.findByExternalOrderId(ifoodOrderId).ifPresentOrElse(order -> {
            Conversation conversation = conversationRepository.findByStoreOrderChannel(storeId, order.getId(), "IFOOD")
                    .orElse(null);
            if (conversation == null) {
                conversation = new Conversation(storeId, "IFOOD");
                conversation.associateOrder(order.getId(), order.getDisplayId(), order.getCustomerName(),
                        order.getCustomerPhone());
            }
            ConversationMessage message = new ConversationMessage(
                    conversation.getId(),
                    "RESTAURANT".equalsIgnoreCase(sender) ? "OUTBOUND" : "INBOUND",
                    content);
            conversation.addMessage(message);
            conversationRepository.save(conversation);
            LOG.infof("[IFOOD-SUB] Chat salvo: orderId=%s", ifoodOrderId);
        }, () -> LOG.warnf("[IFOOD-SUB] Pedido não encontrado para chat: %s", ifoodOrderId));
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

    private void ensureSubscriptionExists(String subId, String topicId) {
        if (channel == null)
            return;
        try {
            var channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            var credentialsProvider = NoCredentialsProvider.create();

            TopicAdminSettings topicSettings = TopicAdminSettings.newBuilder()
                    .setTransportChannelProvider(channelProvider)
                    .setCredentialsProvider(credentialsProvider)
                    .build();
            try (TopicAdminClient topicClient = TopicAdminClient.create(topicSettings)) {
                ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
                try {
                    topicClient.getTopic(topicName);
                } catch (Exception e) {
                    topicClient.createTopic(Topic.newBuilder().setName(topicName.toString()).build());
                    LOG.infof("[IFOOD-SUB] Tópico criado no emulador: %s", topicId);
                }
            }

            SubscriptionAdminSettings subSettings = SubscriptionAdminSettings.newBuilder()
                    .setTransportChannelProvider(channelProvider)
                    .setCredentialsProvider(credentialsProvider)
                    .build();
            try (SubscriptionAdminClient subClient = SubscriptionAdminClient.create(subSettings)) {
                ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, subId);
                ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
                try {
                    subClient.getSubscription(subName);
                } catch (Exception e) {
                    subClient.createSubscription(Subscription.newBuilder()
                            .setName(subName.toString())
                            .setTopic(topicName.toString())
                            .setPushConfig(PushConfig.getDefaultInstance())
                            .setAckDeadlineSeconds(60)
                            .build());
                    LOG.infof("[IFOOD-SUB] Subscription criada no emulador: %s", subId);
                }
            }
        } catch (Exception e) {
            LOG.warnf("[IFOOD-SUB] Não foi possível verificar/criar subscription no emulador: %s", e.getMessage());
        }
    }

    private Subscriber buildSubscriber(String subscriptionId, MessageReceiver receiver) {
        ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, subscriptionId);

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
