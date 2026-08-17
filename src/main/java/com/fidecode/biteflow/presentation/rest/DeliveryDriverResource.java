package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.delivery.*;
import com.fidecode.biteflow.application.delivery.dto.DriverRequest;
import com.fidecode.biteflow.application.delivery.dto.DriverResponse;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/api/v1/drivers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)


@RolesAllowed({"ADMIN","GERENTE"})
public class DeliveryDriverResource extends BaseResource {

    @Inject CreateDriverUseCase createUseCase;
    @Inject UpdateDriverUseCase updateUseCase;
    @Inject DeleteDriverUseCase deleteUseCase;
    @Inject GetDriverUseCase getUseCase;
    @Inject ListDriversUseCase listUseCase;

    @PermissionsAllowed("entregadores")
    @GET
    public Response list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return Response.ok(listUseCase.execute(getStoreId(), page, size)).build();
    }

    @PermissionsAllowed("entregadores")
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return Response.ok(getUseCase.execute(getStoreId(), id)).build();
    }

    @PermissionsAllowed("entregadores")
    @POST
    public Response create(@Valid DriverRequest request) {
        DriverResponse created = createUseCase.execute(getStoreId(), request);
        return Response.created(URI.create("/api/v1/drivers/" + created.id()))
            .entity(created).build();
    }

    @PermissionsAllowed("entregadores")
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid DriverRequest request) {
        return Response.ok(updateUseCase.execute(getStoreId(), id, request)).build();
    }

    @PermissionsAllowed("entregadores")
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }
}
