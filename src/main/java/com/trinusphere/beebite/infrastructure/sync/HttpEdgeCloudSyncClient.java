package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class HttpEdgeCloudSyncClient implements EdgeCloudSyncClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

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
                String detail = response.body() != null ? response.body().strip() : "";
                if (detail.length() > 180) {
                    detail = detail.substring(0, 180) + "...";
                }
                throw new IllegalStateException("Cloud sync endpoint returned HTTP " + response.statusCode()
                        + (detail.isEmpty() ? "" : ": " + detail));
            }
            return objectMapper.readValue(response.body(), EdgeSyncBatchResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao sincronizar lote com a nuvem: " + e.getMessage(), e);
        }
    }
}
