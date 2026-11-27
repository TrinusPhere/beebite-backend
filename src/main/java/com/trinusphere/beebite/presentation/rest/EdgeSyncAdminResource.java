package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.sync.ListFailedEdgeOutboxEventsUseCase;
import com.trinusphere.beebite.application.sync.RetryFailedEdgeOutboxEventUseCase;
import com.trinusphere.beebite.application.sync.TriggerEdgeReconciliationUseCase;
import com.trinusphere.beebite.application.sync.dto.EdgeOutboxFailedEventResponse;
import com.trinusphere.beebite.application.sync.dto.ReconciliationResult;
import com.trinusphere.beebite.application.sync.dto.TriggerReconciliationRequest;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/sync")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE"})
public class EdgeSyncAdminResource extends BaseResource {

    @Inject
    ListFailedEdgeOutboxEventsUseCase listFailedEventsUseCase;

    @Inject
    RetryFailedEdgeOutboxEventUseCase retryFailedEventUseCase;

    @Inject
    TriggerEdgeReconciliationUseCase triggerReconciliationUseCase;

    @GET
    @Path("/failed")
    @PermissionsAllowed("integracoes")
    public Response listFailed(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        List<EdgeOutboxFailedEventResponse> events = listFailedEventsUseCase.execute(getStoreId(), page, size);
        return Response.ok(events).build();
    }

    @POST
    @Path("/failed/{eventId}/retry")
    @PermissionsAllowed("integracoes")
    public Response retryFailed(@PathParam("eventId") UUID eventId) {
        int requeued = retryFailedEventUseCase.execute(getStoreId(), eventId);
        return Response.accepted(Map.of("requeued", requeued)).build();
    }

    @POST
    @Path("/reconcile")
    @PermissionsAllowed("integracoes")
    public Response reconcile(@Valid TriggerReconciliationRequest request) {
        ReconciliationResult result = triggerReconciliationUseCase.execute(
                request != null ? request.from() : null,
                request != null ? request.to() : null
        );
        return Response.ok(result).build();
    }
}

