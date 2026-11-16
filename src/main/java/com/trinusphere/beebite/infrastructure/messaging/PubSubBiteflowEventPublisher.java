package com.trinusphere.beebite.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinusphere.beebite.application.order.port.BiteflowEventPublisher;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.valueobject.OrderPlatform;
import com.trinusphere.beebite.domain.messaging.entity.Conversation;
import com.trinusphere.beebite.domain.messaging.entity.ConversationMessage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Implementação do port BiteflowEventPublisher usando Google Cloud Pub/Sub. Publica eventos quando o operador muda o
 * status de pedidos iFood.
 *
 * Se Pub/Sub não estiver configurado, o publisher loga um aviso e não falha.
 */
@ApplicationScoped
public class PubSubBiteflowEventPublisher implements BiteflowEventPublisher {

    private static final Logger LOG = Logger.getLogger(PubSubBiteflowEventPublisher.class);
    private static final String TOPIC_STATUS_CHANGED = "beebite-order-status-changed";
    private static final String TOPIC_CANCELLED = "beebite-order-cancelled";
    private static final String TOPIC_CHAT_MESSAGE = "beebite-chat-message-outbound";

    @ConfigProperty(name = "quarkus.google.cloud.project-id", defaultValue = "beebite-dev")
    String projectId;

    @ConfigProperty(name = "biteflow.feature.ifood-integration.enabled", defaultValue = "true")
    boolean ifoodIntegrationEnabled;

    @ConfigProperty(name = "quarkus.google.cloud.pubsub.endpoint")
    Optional<String> emulatorEndpoint;

    @ConfigProperty(name = "quarkus.google.cloud.pubsub.use-plaintext")
    Optional<Boolean> usePlaintext;

    @Inject
    ObjectMapper objectMapper;

    private Publisher statusChangedPublisher;
    private Publisher cancelledPublisher;
    private Publisher chatMessagePublisher;
    private ManagedChannel channel;

    @PostConstruct
    void init() {
        if (!ifoodIntegrationEnabled) {
            LOG.info("[PUBSUB-BF] Integração iFood desabilitada — publishers não inicializados.");
            return;
        }
        try {
            statusChangedPublisher = buildPublisher(TOPIC_STATUS_CHANGED);
            cancelledPublisher = buildPublisher(TOPIC_CANCELLED);
            chatMessagePublisher = buildPublisher(TOPIC_CHAT_MESSAGE);
            LOG.infof("[PUBSUB-BF] Publishers iFood inicializados (project=%s).", projectId);
        } catch (Exception e) {
            LOG.warnf("[PUBSUB-BF] Pub/Sub indisponível — eventos iFood serão ignorados: %s", e.getMessage());
        }
    }

    @PreDestroy
    void shutdown() {
        shutdownPublisher(statusChangedPublisher);
        shutdownPublisher(cancelledPublisher);
        shutdownPublisher(chatMessagePublisher);
        if (channel != null)
            channel.shutdownNow();
    }

    @Override
    public void publishStatusChanged(Order order) {
        if (order.getPlatform() != OrderPlatform.IFOOD || order.getExternalOrderId() == null)
            return;
        if (statusChangedPublisher == null) {
            LOG.debug("[PUBSUB-BF] Publisher não disponível.");
            return;
        }

        try {
            Map<String, Object> event = Map.of("storeId", order.getStoreId().toString(), "orderId",
                    order.getId().toString(), "ifoodOrderId", order.getExternalOrderId(), "newStatus",
                    order.getStatus().name(), "changedAt", Instant.now().toString());
            publish(statusChangedPublisher, event, TOPIC_STATUS_CHANGED);
        } catch (Exception e) {
            LOG.errorf(e, "[PUBSUB-BF] Falha ao publicar status change: %s", e.getMessage());
            // Não propaga — a mudança de status no BiteFlow não pode ser revertida por isso
        }
    }

    @Override
    public void publishCancelled(Order order, String cancellationCode, String reason) {
        if (order.getPlatform() != OrderPlatform.IFOOD || order.getExternalOrderId() == null)
            return;
        if (cancelledPublisher == null) {
            LOG.debug("[PUBSUB-BF] Publisher não disponível.");
            return;
        }

        try {
            Map<String, Object> event = Map.of("storeId", order.getStoreId().toString(), "orderId",
                    order.getId().toString(), "ifoodOrderId", order.getExternalOrderId(), "cancellationCode",
                    cancellationCode, "reason", reason, "cancelledAt", Instant.now().toString());
            publish(cancelledPublisher, event, TOPIC_CANCELLED);
        } catch (Exception e) {
            LOG.errorf(e, "[PUBSUB-BF] Falha ao publicar cancelamento: %s", e.getMessage());
        }
    }

    @Override
    public void publishChatMessageSent(Conversation conversation, ConversationMessage message) {
        if (!"IFOOD".equals(conversation.getChannel()))
            return;
        if (chatMessagePublisher == null) {
            LOG.debug("[PUBSUB-BF] Publisher de chat não disponível.");
            return;
        }

        try {
            Map<String, Object> event = Map.of(
                    "storeId", conversation.getStoreId().toString(),
                    "orderId", conversation.getOrderId() != null ? conversation.getOrderId().toString() : "",
                    "message", message.getContent()
            );
            publish(chatMessagePublisher, event, TOPIC_CHAT_MESSAGE);
            LOG.infof("[PUBSUB-BF] Mensagem de chat publicada: storeId=%s", conversation.getStoreId());
        } catch (Exception e) {
            LOG.errorf(e, "[PUBSUB-BF] Falha ao publicar mensagem de chat: %s", e.getMessage());
        }
    }

    private void publish(Publisher publisher, Object payload, String topic) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(json)).build();
        publisher.publish(message).get(10, TimeUnit.SECONDS);
        LOG.debugf("[PUBSUB-BF] Publicado no tópico: %s", topic);
    }

    private Publisher buildPublisher(String topicId) throws Exception {
        ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);

        if (Boolean.TRUE.equals(usePlaintext.orElse(false)) && emulatorEndpoint.isPresent()) {
            if (channel == null) {
                channel = ManagedChannelBuilder.forTarget(emulatorEndpoint.get()).usePlaintext().build();
            }
            var channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            return Publisher.newBuilder(topicName).setChannelProvider(channelProvider)
                    .setCredentialsProvider(NoCredentialsProvider.create()).build();
        }

        return Publisher.newBuilder(topicName).build();
    }

    private void shutdownPublisher(Publisher publisher) {
        if (publisher != null) {
            try {
                publisher.shutdown();
                publisher.awaitTermination(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }
    }
}
