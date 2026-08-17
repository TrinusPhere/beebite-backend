package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.catalog.dto.ProductRequest;
import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
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

@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class ProductResource {

    @Inject com.fidecode.biteflow.application.catalog.product.CreateProductUseCase createProductUseCase;
    @Inject com.fidecode.biteflow.application.catalog.product.UpdateProductUseCase updateProductUseCase;
    @Inject com.fidecode.biteflow.application.catalog.product.DeleteProductUseCase deleteProductUseCase;
    @Inject com.fidecode.biteflow.application.catalog.product.GetProductUseCase getProductUseCase;
    @Inject com.fidecode.biteflow.application.catalog.product.ListProductsUseCase listProductsUseCase;
    @Inject com.fidecode.biteflow.application.catalog.product.ManageProductModifiersUseCase manageProductModifiersUseCase;

    @Inject JsonWebToken jwt;

    @GET
    public Response list(
            @QueryParam("categoryId") String categoryId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        checkPermission("produtos");
        return Response.ok(listProductsUseCase.execute(getStoreId(), categoryId, page, size)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        checkPermission("produtos");
        return Response.ok(getProductUseCase.execute(getStoreId(), id)).build();
    }

    @POST
    public Response create(@Valid ProductRequest request) {
        checkPermission("produtos");
        ProductResponse created = createProductUseCase.execute(getStoreId(), request);
        return Response.created(URI.create("/api/v1/products/" + created.id()))
            .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid ProductRequest request) {
        checkPermission("produtos");
        return Response.ok(updateProductUseCase.execute(getStoreId(), id, request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkPermission("produtos");
        deleteProductUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/removable-items")
    public Response updateRemovableItems(@PathParam("id") UUID id, List<String> ingredientIds) {
        checkPermission("produtos");
        return Response.ok(manageProductModifiersUseCase.updateRemovableItems(getStoreId(), id, ingredientIds)).build();
    }

    @PUT
    @Path("/{id}/modifier-groups")
    public Response updateModifierGroups(@PathParam("id") UUID id, List<String> groupIds) {
        checkPermission("produtos");
        return Response.ok(manageProductModifiersUseCase.updateModifierGroups(getStoreId(), id, groupIds)).build();
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
