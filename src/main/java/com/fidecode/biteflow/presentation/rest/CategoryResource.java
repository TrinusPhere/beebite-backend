package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.catalog.category.*;
import com.fidecode.biteflow.application.catalog.dto.CategoryRequest;
import com.fidecode.biteflow.application.catalog.dto.CategoryResponse;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE"})
public class CategoryResource extends BaseResource {

    @Inject CreateCategoryUseCase createCategoryUseCase;
    @Inject UpdateCategoryUseCase updateCategoryUseCase;
    @Inject DeleteCategoryUseCase deleteCategoryUseCase;
    @Inject GetCategoryUseCase getCategoryUseCase;
    @Inject ListCategoriesUseCase listCategoriesUseCase;
    @Inject BulkApplyCategoryModifiersUseCase bulkApplyCategoryModifiersUseCase;

    @POST
    @PermissionsAllowed("produtos")
    public Response create(@Valid CategoryRequest request) {
        CategoryResponse response = createCategoryUseCase.execute(getStoreId(), request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @PermissionsAllowed("produtos")
    public Response update(@PathParam("id") UUID id, @Valid CategoryRequest request) {
        CategoryResponse response = updateCategoryUseCase.execute(getStoreId(), id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @PermissionsAllowed("produtos")
    public Response delete(@PathParam("id") UUID id) {
        deleteCategoryUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }

    @GET
    @PermissionsAllowed("produtos")
    public Response list() {
        List<CategoryResponse> list = listCategoriesUseCase.execute(getStoreId());
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    @PermissionsAllowed("produtos")
    public Response getById(@PathParam("id") UUID id) {
        CategoryResponse response = getCategoryUseCase.execute(getStoreId(), id);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/bulk-apply-modifiers")
    @PermissionsAllowed("produtos")
    public Response bulkApplyModifiers(@PathParam("id") UUID id) {
        bulkApplyCategoryModifiersUseCase.execute(getStoreId(), id);
        return Response.ok().build();
    }
}
