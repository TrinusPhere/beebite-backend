package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.catalog.*;
import com.fidecode.biteflow.application.catalog.dto.IngredientRequest;
import com.fidecode.biteflow.application.catalog.dto.IngredientResponse;
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

@Path("/api/v1/ingredients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class IngredientResource {

    @Inject CreateIngredientUseCase createUseCase;
    @Inject UpdateIngredientUseCase updateUseCase;
    @Inject DeleteIngredientUseCase deleteUseCase;
    @Inject ListIngredientsUseCase listUseCase;
    @Inject GetIngredientUseCase getUseCase;
    @Inject JsonWebToken jwt;

    @GET
    public Response list() {
        checkPermission("ingredientes");
        UUID storeId = getStoreId();
        List<IngredientResponse> ingredients = listUseCase.execute(storeId);
        return Response.ok(ingredients).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        checkPermission("ingredientes");
        return Response.ok(getUseCase.execute(id)).build();
    }

    @POST
    public Response create(@Valid IngredientRequest request) {
        checkPermission("ingredientes");
        UUID storeId = getStoreId();
        IngredientResponse created = createUseCase.execute(storeId, request);
        return Response.created(URI.create("/api/v1/ingredients/" + created.id()))
            .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid IngredientRequest request) {
        checkPermission("ingredientes");
        return Response.ok(updateUseCase.execute(id, request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkPermission("ingredientes");
        deleteUseCase.execute(id);
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
