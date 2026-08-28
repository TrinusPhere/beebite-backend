package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.inventory.*;
import com.fidecode.biteflow.application.inventory.dto.StockMovementRequest;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/api/v1/stock-movements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE" })
public class StockMovementResource extends BaseResource {
    @Inject
    ListStockMovementsUseCase listUseCase;
    @Inject
    CreateStockMovementUseCase createUseCase;

    @GET
    @PermissionsAllowed("ingredientes")
    public Response list() {
        return Response.ok(listUseCase.execute(getStoreId())).build();
    }

    @POST
    @PermissionsAllowed("ingredientes")
    public Response create(@Valid StockMovementRequest r) {
        return Response.status(Response.Status.CREATED).entity(createUseCase.execute(getStoreId(), r)).build();
    }
}
