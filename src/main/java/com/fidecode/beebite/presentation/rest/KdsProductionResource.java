package com.fidecode.beebite.presentation.rest;

import com.fidecode.beebite.application.kds.*;
import com.fidecode.beebite.application.kds.dto.ProductionTaskResponse;
import com.fidecode.beebite.infrastructure.kds.KdsEventBroadcaster;
import com.fidecode.beebite.infrastructure.kds.KdsEventPayload;
import io.quarkus.security.PermissionsAllowed;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/kds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE", "CAIXA"})
public class KdsProductionResource extends BaseResource {

    private final ListActiveProductionTasksUseCase listActiveUseCase;
    private final ListCompletedProductionTasksUseCase listCompletedUseCase;
    private final DispatchOrderToKdsUseCase dispatchUseCase;
    private final StartProductionTaskUseCase startTaskUseCase;
    private final CompleteProductionTaskUseCase completeTaskUseCase;
    private final ReopenProductionTaskUseCase reopenTaskUseCase;
    private final KdsEventBroadcaster eventBroadcaster;

    @Inject
    public KdsProductionResource(ListActiveProductionTasksUseCase listActiveUseCase,
                                 ListCompletedProductionTasksUseCase listCompletedUseCase,
                                 DispatchOrderToKdsUseCase dispatchUseCase,
                                 StartProductionTaskUseCase startTaskUseCase,
                                 CompleteProductionTaskUseCase completeTaskUseCase,
                                 ReopenProductionTaskUseCase reopenTaskUseCase,
                                 KdsEventBroadcaster eventBroadcaster) {
        this.listActiveUseCase = listActiveUseCase;
        this.listCompletedUseCase = listCompletedUseCase;
        this.dispatchUseCase = dispatchUseCase;
        this.startTaskUseCase = startTaskUseCase;
        this.completeTaskUseCase = completeTaskUseCase;
        this.reopenTaskUseCase = reopenTaskUseCase;
        this.eventBroadcaster = eventBroadcaster;
    }

    @GET
    @Path("/tasks/active")
    @PermissionsAllowed("kds")
    public List<ProductionTaskResponse> listActive(@QueryParam("stationId") UUID stationId) {
        return listActiveUseCase.execute(getStoreId(), stationId);
    }

    @GET
    @Path("/tasks/completed")
    @PermissionsAllowed("kds")
    public List<ProductionTaskResponse> listCompleted(@QueryParam("stationId") UUID stationId,
                                                      @QueryParam("limit") @DefaultValue("50") int limit) {
        return listCompletedUseCase.execute(getStoreId(), stationId, limit);
    }

    @POST
    @Path("/orders/{orderId}/dispatch")
    @PermissionsAllowed("kds")
    public List<ProductionTaskResponse> dispatchOrder(@PathParam("orderId") UUID orderId) {
        return dispatchUseCase.execute(getStoreId(), orderId);
    }

    @PATCH
    @Path("/tasks/{id}/start")
    @PermissionsAllowed("kds")
    public ProductionTaskResponse startTask(@PathParam("id") UUID id) {
        return startTaskUseCase.execute(getStoreId(), id);
    }

    @PATCH
    @Path("/tasks/{id}/complete")
    @PermissionsAllowed("kds")
    public ProductionTaskResponse completeTask(@PathParam("id") UUID id) {
        return completeTaskUseCase.execute(getStoreId(), id);
    }

    @PATCH
    @Path("/tasks/{id}/reopen")
    @PermissionsAllowed("kds")
    public ProductionTaskResponse reopenTask(@PathParam("id") UUID id) {
        return reopenTaskUseCase.execute(getStoreId(), id);
    }

    @GET
    @Path("/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    @PermissionsAllowed("kds")
    public Multi<KdsEventPayload> streamEvents() {
        return eventBroadcaster.getStream(getStoreId());
    }
}
