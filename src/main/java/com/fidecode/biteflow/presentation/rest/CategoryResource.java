package com.fidecode.biteflow.presentation.rest;

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

    @Inject com.fidecode.biteflow.application.catalog.category.CreateCategoryUseCase createCategoryUseCase;
    @Inject com.fidecode.biteflow.application.catalog.category.UpdateCategoryUseCase updateCategoryUseCase;
    @Inject com.fidecode.biteflow.application.catalog.category.DeleteCategoryUseCase deleteCategoryUseCase;
    @Inject com.fidecode.biteflow.application.catalog.category.GetCategoryUseCase getCategoryUseCase;
    @Inject com.fidecode.biteflow.application.catalog.category.ListCategoriesUseCase listCategoriesUseCase;
    @Inject com.fidecode.biteflow.application.catalog.category.BulkApplyCategoryModifiersUseCase bulkApplyCategoryModifiersUseCase;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    public Response create(@Valid CategoryRequest request) {
        CategoryResponse response = createCategoryUseCase.execute(getStoreId(), request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response update(@PathParam("id") UUID id, @Valid CategoryRequest request) {
        CategoryResponse response = updateCategoryUseCase.execute(getStoreId(), id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response delete(@PathParam("id") UUID id) {
        deleteCategoryUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    public Response list() {
        List<CategoryResponse> list = listCategoriesUseCase.execute(getStoreId());
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response getById(@PathParam("id") UUID id) {
        CategoryResponse response = getCategoryUseCase.execute(getStoreId(), id);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/bulk-apply-modifiers")
    @RolesAllowed({"ADMIN", "USER"})
    public Response bulkApplyModifiers(@PathParam("id") UUID id) {
        bulkApplyCategoryModifiersUseCase.execute(getStoreId(), id);
        return Response.ok().build();
    }

    private UUID getStoreId() {
        String storeIdClaim = jwt.getClaim("storeId");
        return storeIdClaim != null ? UUID.fromString(storeIdClaim)
            : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
