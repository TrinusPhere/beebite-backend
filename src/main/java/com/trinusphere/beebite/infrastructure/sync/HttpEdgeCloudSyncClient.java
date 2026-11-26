package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
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
        try {
            String body = objectMapper.writeValueAsString(batchRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(cloudBaseUrl + "/api/v1/internal/edge-sync"))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("X-Edge-Device-Token", deviceToken != null ? deviceToken : "")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Cloud sync endpoint returned HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), EdgeSyncBatchResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao sincronizar lote com a nuvem: " + e.getMessage(), e);
        }
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
}
