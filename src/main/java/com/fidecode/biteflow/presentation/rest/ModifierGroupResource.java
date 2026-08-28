package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.catalog.*;
import com.fidecode.biteflow.application.catalog.dto.ModifierGroupRequest;
import com.fidecode.biteflow.application.catalog.dto.ModifierGroupResponse;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/api/v1/modifier-groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@RolesAllowed({ "ADMIN", "GERENTE" })
public class ModifierGroupResource extends BaseResource {

    @Inject
    CreateModifierGroupUseCase createUseCase;
    @Inject
    UpdateModifierGroupUseCase updateUseCase;
    @Inject
    DeleteModifierGroupUseCase deleteUseCase;
    @Inject
    GetModifierGroupUseCase getUseCase;
    @Inject
    ListModifierGroupsUseCase listUseCase;

    @PermissionsAllowed("produtos")
    @GET
    public Response list(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return Response.ok(listUseCase.execute(getStoreId(), page, size)).build();
    }

    @PermissionsAllowed("produtos")
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return Response.ok(getUseCase.execute(getStoreId(), id)).build();
    }

    @PermissionsAllowed("produtos")
    @POST
    public Response create(@Valid ModifierGroupRequest request) {
        ModifierGroupResponse created = createUseCase.execute(getStoreId(), request);
        return Response.created(URI.create("/api/v1/modifier-groups/" + created.id())).entity(created).build();
    }

    @PermissionsAllowed("produtos")
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid ModifierGroupRequest request) {
        return Response.ok(updateUseCase.execute(getStoreId(), id, request)).build();
    }

    @PermissionsAllowed("produtos")
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }
}
