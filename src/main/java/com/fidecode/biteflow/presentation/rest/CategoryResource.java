package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.catalog.CategoryUseCases;
import com.fidecode.biteflow.application.catalog.dto.CategoryRequest;
import com.fidecode.biteflow.application.catalog.dto.CategoryResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    CategoryUseCases categoryUseCases;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    public Response create(@Valid CategoryRequest request) {
        UUID storeId = UUID.fromString(jwt.getClaim("storeId"));
        CategoryResponse response = categoryUseCases.create(storeId, request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response update(@PathParam("id") UUID id, @Valid CategoryRequest request) {
        CategoryResponse response = categoryUseCases.update(id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response delete(@PathParam("id") UUID id) {
        categoryUseCases.delete(id);
        return Response.noContent().build();
    }

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    public Response list() {
        UUID storeId = UUID.fromString(jwt.getClaim("storeId"));
        List<CategoryResponse> list = categoryUseCases.list(storeId);
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response getById(@PathParam("id") UUID id) {
        CategoryResponse response = categoryUseCases.getById(id);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/bulk-apply-modifiers")
    @RolesAllowed({"ADMIN", "USER"})
    public Response bulkApplyModifiers(@PathParam("id") UUID id) {
        categoryUseCases.bulkApplyModifiers(id);
        return Response.ok().build();
    }
}
