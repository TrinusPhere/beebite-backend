package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.catalog.dto.ProductRequest;
import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.application.catalog.product.*;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@RolesAllowed({ "ADMIN", "GERENTE" })
public class ProductResource extends BaseResource {

    @Inject
    CreateProductUseCase createProductUseCase;
    @Inject
    UpdateProductUseCase updateProductUseCase;
    @Inject
    DeleteProductUseCase deleteProductUseCase;
    @Inject
    GetProductUseCase getProductUseCase;
    @Inject
    ListProductsUseCase listProductsUseCase;
    @Inject
    UpdateProductRemovableItemsUseCase updateProductRemovableItemsUseCase;
    @Inject
    UpdateProductModifierGroupsUseCase updateProductModifierGroupsUseCase;
    @Inject
    GetProductByExternalCodeUseCase getProductByExternalCodeUseCase;
    @Inject
    UpdateProductAvailabilityUseCase updateProductAvailabilityUseCase;

    @PermissionsAllowed("produtos")
    @GET
    public Response list(@QueryParam("categoryId") String categoryId, @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(listProductsUseCase.execute(getStoreId(), categoryId, page, size)).build();
    }

    @PermissionsAllowed("produtos")
    @GET
    @Path("/by-external-code/{externalCode}")
    public Response getByExternalCode(@PathParam("externalCode") String externalCode) {
        ProductResponse product = getProductByExternalCodeUseCase.execute(getStoreId(), externalCode);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }

    @PermissionsAllowed("produtos")
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return Response.ok(getProductUseCase.execute(getStoreId(), id)).build();
    }

    @PermissionsAllowed("produtos")
    @POST
    public Response create(@Valid ProductRequest request) {
        ProductResponse created = createProductUseCase.execute(getStoreId(), request);
        return Response.created(URI.create("/api/v1/products/" + created.id())).entity(created).build();
    }

    @PermissionsAllowed("produtos")
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid ProductRequest request) {
        return Response.ok(updateProductUseCase.execute(getStoreId(), id, request)).build();
    }

    @PermissionsAllowed("produtos")
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteProductUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }

    @PermissionsAllowed("produtos")
    @PUT
    @Path("/{id}/removable-items")
    public Response updateRemovableItems(@PathParam("id") UUID id, List<String> ingredientIds) {
        return Response.ok(updateProductRemovableItemsUseCase.execute(getStoreId(), id, ingredientIds)).build();
    }

    @PermissionsAllowed("produtos")
    @PUT
    @Path("/{id}/modifier-groups")
    public Response updateModifierGroups(@PathParam("id") UUID id, List<String> groupIds) {
        return Response.ok(updateProductModifierGroupsUseCase.execute(getStoreId(), id, groupIds)).build();
    }

    @PermissionsAllowed("produtos")
    @PUT
    @Path("/{id}/availability")
    public Response updateAvailability(@PathParam("id") UUID id, @Valid com.fidecode.biteflow.application.catalog.dto.ProductAvailabilityRequest request) {
        return Response.ok(updateProductAvailabilityUseCase.execute(getStoreId(), id, request)).build();
    }
}
