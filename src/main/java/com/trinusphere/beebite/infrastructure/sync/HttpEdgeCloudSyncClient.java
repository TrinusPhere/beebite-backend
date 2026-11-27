package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class HttpEdgeCloudSyncClient implements EdgeCloudSyncClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final ObjectMapper objectMapper;
    private final String cloudBaseUrl;
    private final HttpClient httpClient;

    @Inject
    public HttpEdgeCloudSyncClient(
            ObjectMapper objectMapper,
            @ConfigProperty(name = "biteflow.sync.cloud-url", defaultValue = "https://api.trinusphere.com") String cloudBaseUrl) {
        this.objectMapper = objectMapper;
        this.cloudBaseUrl = cloudBaseUrl != null ? cloudBaseUrl.replaceAll("/+$", "") : "";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    @Override
    public EdgeSyncBatchResponse sendBatch(EdgeSyncBatchRequest batchRequest, String deviceToken) {
        return postJson(
                "/api/v1/internal/edge-sync",
                batchRequest,
                deviceToken,
                EdgeSyncBatchResponse.class,
                "Falha ao sincronizar lote com a nuvem"
        );
    }

    @Override
    public EdgeCatalogDeltaResponse pullCatalogDelta(UUID storeId, Instant since, String deviceToken) {
        try {
            StringBuilder url = new StringBuilder(cloudBaseUrl)
                    .append("/api/v1/internal/edge-sync/catalog?storeId=")
                    .append(URLEncoder.encode(storeId.toString(), StandardCharsets.UTF_8));
            if (since != null) {
                url.append("&since=").append(URLEncoder.encode(since.toString(), StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString()))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .header("X-Edge-Device-Token", deviceToken != null ? deviceToken : "")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Cloud catalog delta endpoint returned HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), EdgeCatalogDeltaResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao puxar delta de cardápio da nuvem: " + e.getMessage(), e);
        }
    }

    @Override
    public EdgeReconcileBucketsResponse compareBuckets(EdgeReconcileBucketsRequest request, String deviceToken) {
        return postJson(
                "/api/v1/internal/edge-sync/reconcile/buckets",
                request,
                deviceToken,
                EdgeReconcileBucketsResponse.class,
                "Falha ao comparar resumo de reconciliação com a nuvem"
        );
    }

    @Override
    public EdgeReconcileItemsResponse compareItems(EdgeReconcileItemsRequest request, String deviceToken) {
        return postJson(
                "/api/v1/internal/edge-sync/reconcile/items",
                request,
                deviceToken,
                EdgeReconcileItemsResponse.class,
                "Falha ao comparar itens de reconciliação com a nuvem"
        );
    }

    private <T> T postJson(String path, Object payload, String deviceToken, Class<T> responseType, String errorPrefix) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(cloudBaseUrl + path))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("X-Edge-Device-Token", deviceToken != null ? deviceToken : "")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Endpoint " + path + " retornou HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (Exception e) {
            throw new RuntimeException(errorPrefix + ": " + e.getMessage(), e);
        }
    }
}
