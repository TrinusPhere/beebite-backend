package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.delivery.DriverUseCases;
import com.fidecode.biteflow.application.delivery.dto.DriverRequest;
import com.fidecode.biteflow.application.delivery.dto.DriverResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/drivers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class DeliveryDriverResource {

    @Inject DriverUseCases driverUseCases;
    @Inject JsonWebToken jwt;

    @GET
    public Response list() {
        checkPermission("entregadores");
        return Response.ok(driverUseCases.list(getStoreId())).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        checkPermission("entregadores");
        return Response.ok(driverUseCases.getById(id)).build();
    }

    @POST
    public Response create(@Valid DriverRequest request) {
        checkPermission("entregadores");
        DriverResponse created = driverUseCases.create(getStoreId(), request);
        return Response.created(URI.create("/api/v1/drivers/" + created.id()))
            .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid DriverRequest request) {
        checkPermission("entregadores");
        return Response.ok(driverUseCases.update(id, request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkPermission("entregadores");
        driverUseCases.delete(id);
        return Response.noContent().build();
    }

    private UUID getStoreId() {
        String storeIdClaim = jwt.getClaim("storeId");
        return storeIdClaim != null ? UUID.fromString(storeIdClaim)
            : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private void checkPermission(String required) {
        List<String> permissions = jwt.getClaim("permissions");
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        if (!isAdmin && (permissions == null || !permissions.contains(required))) {
            throw new ForbiddenException("Permissão '" + required + "' necessária.");
        }
    }
}
