package com.fidecode.beebite.presentation.rest;

import com.fidecode.beebite.application.sync.IngestEdgeSyncBatchUseCase;
import com.fidecode.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.fidecode.beebite.application.sync.dto.EdgeSyncBatchResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/internal/edge-sync")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EdgeSyncResource extends BaseResource {

    @Inject
    IngestEdgeSyncBatchUseCase ingestEdgeSyncBatchUseCase;

    @POST
    @PermitAll
    public Response ingestBatch(
            @HeaderParam("X-Edge-Device-Token") String deviceToken,
            EdgeSyncBatchRequest request) {
        EdgeSyncBatchResponse response = ingestEdgeSyncBatchUseCase.execute(deviceToken, request);
        return Response.ok(response).build();
    }
}
