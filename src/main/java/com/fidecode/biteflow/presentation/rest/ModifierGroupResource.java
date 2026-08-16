package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.catalog.ModifierGroupUseCases;
import com.fidecode.biteflow.application.catalog.dto.ModifierGroupRequest;
import com.fidecode.biteflow.application.catalog.dto.ModifierGroupResponse;
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

@Path("/api/v1/modifier-groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class ModifierGroupResource {

    @Inject ModifierGroupUseCases useCases;
    @Inject JsonWebToken jwt;

    @GET
    public Response list() {
        checkPermission("produtos");
        return Response.ok(useCases.list(getStoreId())).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        checkPermission("produtos");
        return Response.ok(useCases.getById(id)).build();
    }

    @POST
    public Response create(@Valid ModifierGroupRequest request) {
        checkPermission("produtos");
        ModifierGroupResponse created = useCases.create(getStoreId(), request);
        return Response.created(URI.create("/api/v1/modifier-groups/" + created.id()))
            .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid ModifierGroupRequest request) {
        checkPermission("produtos");
        return Response.ok(useCases.update(id, request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkPermission("produtos");
        useCases.delete(id);
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
