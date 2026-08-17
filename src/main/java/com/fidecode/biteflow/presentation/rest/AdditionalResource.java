package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.catalog.AdditionalUseCases;
import com.fidecode.biteflow.application.catalog.dto.AdditionalRequest;
import com.fidecode.biteflow.application.catalog.dto.AdditionalResponse;
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

@Path("/api/v1/additionals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class AdditionalResource {

    @Inject AdditionalUseCases useCases;
    @Inject JsonWebToken jwt;

    @GET
    public Response list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        checkPermission("produtos");
        return Response.ok(useCases.list(getStoreId(), page, size)).build();
    }

    @POST
    public Response create(@Valid AdditionalRequest request) {
        checkPermission("produtos");
        AdditionalResponse created = useCases.create(getStoreId(), request);
        return Response.created(URI.create("/api/v1/additionals/" + created.id()))
            .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AdditionalRequest request) {
        checkPermission("produtos");
        return Response.ok(useCases.update(getStoreId(), id, request)).build();
    }

    @PATCH
    @Path("/{id}/toggle-status")
    public Response toggleStatus(@PathParam("id") UUID id) {
        checkPermission("produtos");
        return Response.ok(useCases.toggleStatus(getStoreId(), id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkPermission("produtos");
        useCases.delete(getStoreId(), id);
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
