package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.catalog.*;
import com.trinusphere.beebite.application.catalog.dto.IngredientRequest;
import com.trinusphere.beebite.application.catalog.dto.IngredientResponse;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/api/v1/ingredients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@RolesAllowed({ "ADMIN", "GERENTE" })
public class IngredientResource extends BaseResource {

    @Inject
    CreateIngredientUseCase createUseCase;
    @Inject
    UpdateIngredientUseCase updateUseCase;
    @Inject
    DeleteIngredientUseCase deleteUseCase;
    @Inject
    ListIngredientsUseCase listUseCase;
    @Inject
    GetIngredientUseCase getUseCase;

    @PermissionsAllowed("ingredientes")
    @GET
    public Response list(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return Response.ok(listUseCase.execute(getStoreId(), page, size)).build();
    }

    @PermissionsAllowed("ingredientes")
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return Response.ok(getUseCase.execute(getStoreId(), id)).build();
    }

    @PermissionsAllowed("ingredientes")
    @POST
    public Response create(@Valid IngredientRequest request) {
        IngredientResponse created = createUseCase.execute(getStoreId(), request);
        return Response.created(URI.create("/api/v1/ingredients/" + created.id())).entity(created).build();
    }

    @PermissionsAllowed("ingredientes")
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid IngredientRequest request) {
        return Response.ok(updateUseCase.execute(getStoreId(), id, request)).build();
    }

    @PermissionsAllowed("ingredientes")
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }
}
