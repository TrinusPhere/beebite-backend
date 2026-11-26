package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.sync.GetEdgeCatalogDeltaUseCase;
import com.trinusphere.beebite.application.sync.IngestEdgeSyncBatchUseCase;
import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.domain.sync.exception.InvalidSyncRequestException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Path("/api/v1/internal/edge-sync")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EdgeSyncResource extends BaseResource {

    @Inject
    IngestEdgeSyncBatchUseCase ingestEdgeSyncBatchUseCase;

    @Inject
    GetEdgeCatalogDeltaUseCase getEdgeCatalogDeltaUseCase;

    @POST
    @PermitAll
    public Response ingestBatch(
            @HeaderParam("X-Edge-Device-Token") String deviceToken,
            EdgeSyncBatchRequest request) {
        EdgeSyncBatchResponse response = ingestEdgeSyncBatchUseCase.execute(deviceToken, request);
        return Response.ok(response).build();
    }

    @GET
    @Path("/catalog")
    @PermitAll
    public Response pullCatalogDelta(
            @HeaderParam("X-Edge-Device-Token") String deviceToken,
            @QueryParam("storeId") UUID storeId,
            @QueryParam("since") String sinceRaw) {
        Instant since = parseSince(sinceRaw);
        EdgeCatalogDeltaResponse response = getEdgeCatalogDeltaUseCase.execute(deviceToken, storeId, since);
        return Response.ok(response).build();
    }

    private Instant parseSince(String sinceRaw) {
        if (sinceRaw == null || sinceRaw.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(sinceRaw.trim());
        } catch (DateTimeParseException ex) {
            throw new InvalidSyncRequestException("Parâmetro 'since' inválido; esperado timestamp ISO-8601 UTC.");
        }
    }
}
