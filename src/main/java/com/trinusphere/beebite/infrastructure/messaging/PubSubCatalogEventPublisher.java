package com.trinusphere.beebite.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinusphere.beebite.application.catalog.port.CatalogEventPublisher;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

@ApplicationScoped
public class PubSubCatalogEventPublisher implements CatalogEventPublisher {

    private static final Logger LOG = Logger.getLogger(PubSubCatalogEventPublisher.class);
    private static final String TOPIC_CATALOG_CHANGED = "beebite-catalog-changed";

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

    private Publisher catalogChangedPublisher;
    private ManagedChannel channel;

    @PostConstruct
    void init() {
        if (!ifoodIntegrationEnabled) {
            LOG.info("[PUBSUB-CATALOG] Integração iFood desabilitada — publisher de catálogo não inicializado.");
            return;
        }
        try {
            catalogChangedPublisher = buildPublisher(TOPIC_CATALOG_CHANGED);
            LOG.infof("[PUBSUB-CATALOG] Publisher de catálogo inicializado (project=%s).", projectId);
        } catch (Exception e) {
            LOG.warnf("[PUBSUB-CATALOG] Pub/Sub indisponível — eventos de catálogo serão ignorados: %s", e.getMessage());
        }
    }

    @PreDestroy
    void shutdown() {
        if (catalogChangedPublisher != null) {
            try {
                catalogChangedPublisher.shutdown();
                catalogChangedPublisher.awaitTermination(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }
        if (channel != null)
            channel.shutdownNow();
    }

    @Override
    public void publishCatalogChanged(UUID catalogId, UUID storeId, UUID productId, String changeType,
            BigDecimal effectiveIfoodPrice, String globalStatus) {
        if (catalogChangedPublisher == null) {
            LOG.debug("[PUBSUB-CATALOG] Publisher não disponível.");
            return;
        }

        try {
            Map<String, Object> event = new LinkedHashMap<>();
            if (catalogId != null) {
                event.put("catalogId", catalogId.toString());
            }
            if (storeId != null) {
                event.put("storeId", storeId.toString());
            }
            event.put("productId", productId.toString());
            event.put("changeType", changeType);
            if (effectiveIfoodPrice != null) {
                event.put("effectiveIfoodPrice", effectiveIfoodPrice);
            }
            if (globalStatus != null) {
                event.put("globalStatus", globalStatus);
            }

            String json = objectMapper.writeValueAsString(event);
            PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(json)).build();
            catalogChangedPublisher.publish(message).get(10, TimeUnit.SECONDS);
            LOG.debugf("[PUBSUB-CATALOG] Publicado evento %s para catalog=%s product=%s", changeType, catalogId, productId);
        } catch (Exception e) {
            LOG.errorf(e, "[PUBSUB-CATALOG] Falha ao publicar alteração de catálogo: %s", e.getMessage());
        }
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
}
