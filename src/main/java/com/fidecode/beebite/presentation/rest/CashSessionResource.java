package com.fidecode.beebite.presentation.rest;

import com.fidecode.beebite.application.cash.*;
import com.fidecode.beebite.application.cash.dto.*;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/cash/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE", "CAIXA"})
public class CashSessionResource extends BaseResource {

    private final OpenCashSessionUseCase openSessionUseCase;
    private final GetCurrentCashSessionUseCase getCurrentSessionUseCase;
    private final CloseCashSessionUseCase closeSessionUseCase;
    private final GetCashSessionSummaryUseCase getSessionSummaryUseCase;
    private final AddCashMovementUseCase addMovementUseCase;
    private final ListCashMovementsUseCase listMovementsUseCase;

    @Inject
    public CashSessionResource(OpenCashSessionUseCase openSessionUseCase,
                               GetCurrentCashSessionUseCase getCurrentSessionUseCase,
                               CloseCashSessionUseCase closeSessionUseCase,
                               GetCashSessionSummaryUseCase getSessionSummaryUseCase,
                               AddCashMovementUseCase addMovementUseCase,
                               ListCashMovementsUseCase listMovementsUseCase) {
        this.openSessionUseCase = openSessionUseCase;
        this.getCurrentSessionUseCase = getCurrentSessionUseCase;
        this.closeSessionUseCase = closeSessionUseCase;
        this.getSessionSummaryUseCase = getSessionSummaryUseCase;
        this.addMovementUseCase = addMovementUseCase;
        this.listMovementsUseCase = listMovementsUseCase;
    }

    @POST
    @Path("/open")
    @PermissionsAllowed("caixa")
    public Response openSession(@Valid OpenCashSessionRequest request) {
        CashSessionResponse response = openSessionUseCase.execute(getStoreId(), getUserId(), request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/current")
    @PermissionsAllowed("caixa")
    public CashSessionResponse getCurrentSession() {
        return getCurrentSessionUseCase.execute(getStoreId(), getUserId());
    }

    @GET
    @Path("/{id}/summary")
    @PermissionsAllowed("caixa")
    public CashSessionSummaryResponse getSummary(@PathParam("id") UUID sessionId) {
        return getSessionSummaryUseCase.execute(getStoreId(), sessionId);
    }

    @POST
    @Path("/{id}/close")
    @PermissionsAllowed("caixa")
    public CashSessionResponse closeSession(@PathParam("id") UUID sessionId,
                                            @Valid CloseCashSessionRequest request) {
        return closeSessionUseCase.execute(getStoreId(), sessionId, request);
    }

    @POST
    @Path("/{id}/movements")
    @PermissionsAllowed("caixa")
    public Response addMovement(@PathParam("id") UUID sessionId,
                                @Valid CashMovementRequest request) {
        CashMovementResponse response = addMovementUseCase.execute(getStoreId(), sessionId, getUserId(), request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}/movements")
    @PermissionsAllowed("caixa")
    public List<CashMovementResponse> listMovements(@PathParam("id") UUID sessionId) {
        return listMovementsUseCase.execute(getStoreId(), sessionId);
    }
}
