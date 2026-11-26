package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.catalog.CreateModifierGroupUseCase;
import com.trinusphere.beebite.application.catalog.ListModifierGroupsUseCase;
import com.trinusphere.beebite.application.catalog.category.CreateCategoryUseCase;
import com.trinusphere.beebite.application.catalog.category.ListCategoriesUseCase;
import com.trinusphere.beebite.application.catalog.dto.CategoryRequest;
import com.trinusphere.beebite.application.catalog.dto.CategoryResponse;
import com.trinusphere.beebite.application.catalog.dto.ModifierGroupRequest;
import com.trinusphere.beebite.application.catalog.dto.ModifierGroupResponse;
import com.trinusphere.beebite.application.catalog.dto.ProductRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductResponse;
import com.trinusphere.beebite.application.catalog.product.CreateProductUseCase;
import com.trinusphere.beebite.application.catalog.product.GetProductByExternalCodeUseCase;
import com.trinusphere.beebite.application.catalog.product.GetProductUseCase;
import com.trinusphere.beebite.application.catalog.product.ListProductsUseCase;
import com.trinusphere.beebite.application.catalog.product.UpdateProductUseCase;
import com.trinusphere.beebite.application.sync.GetInternalStoreCatalogContextUseCase;
import com.trinusphere.beebite.application.sync.dto.InternalStoreCatalogContextResponse;
import com.trinusphere.beebite.domain.sync.exception.InvalidInternalServiceTokenException;
import com.trinusphere.beebite.domain.sync.exception.InvalidSyncRequestException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

/**
 * Canal interno machine-to-machine (autenticado via {@code X-Internal-Service-Token}) utilizado pelo
 * {@code beebite-ifood-service} para consultar contexto de catálogo/loja e sincronizar itens de
 * cardápio tanto via requisições REST quanto em workers background do Google Cloud Pub/Sub.
 */
@Path("/api/v1/internal/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class InternalCatalogSyncResource extends BaseResource {

    @ConfigProperty(name = "biteflow.internal.service-token", defaultValue = "beebite-internal-dev-token")
    String expectedServiceToken;

    @Inject
    GetInternalStoreCatalogContextUseCase getInternalStoreCatalogContextUseCase;
    @Inject
    ListCategoriesUseCase listCategoriesUseCase;
    @Inject
    CreateCategoryUseCase createCategoryUseCase;
    @Inject
    ListProductsUseCase listProductsUseCase;
    @Inject
    GetProductUseCase getProductUseCase;
    @Inject
    GetProductByExternalCodeUseCase getProductByExternalCodeUseCase;
    @Inject
    CreateProductUseCase createProductUseCase;
    @Inject
    UpdateProductUseCase updateProductUseCase;
    @Inject
    ListModifierGroupsUseCase listModifierGroupsUseCase;
    @Inject
    CreateModifierGroupUseCase createModifierGroupUseCase;

    @GET
    @Path("/stores/{storeId}/context")
    public Response getStoreCatalogContext(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @PathParam("storeId") UUID storeId) {
        verifyServiceToken(serviceToken);
        InternalStoreCatalogContextResponse ctx = getInternalStoreCatalogContextUseCase.execute(requireStoreId(storeId));
        return Response.ok(ctx).build();
    }

    @GET
    @Path("/categories")
    public Response listCategories(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId) {
        verifyServiceToken(serviceToken);
        List<CategoryResponse> categories = listCategoriesUseCase.execute(requireStoreId(storeId));
        return Response.ok(categories).build();
    }

    @POST
    @Path("/categories")
    public Response createCategory(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId,
            @Valid CategoryRequest request) {
        verifyServiceToken(serviceToken);
        CategoryResponse created = createCategoryUseCase.execute(requireStoreId(storeId), request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/products")
    public Response listProducts(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId,
            @QueryParam("categoryId") String categoryId,
            @QueryParam("pendingCost") @DefaultValue("false") boolean pendingCost,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {
        verifyServiceToken(serviceToken);
        return Response.ok(listProductsUseCase.execute(requireStoreId(storeId), categoryId, pendingCost, page, size)).build();
    }

    @GET
    @Path("/products/by-external-code/{externalCode}")
    public Response getProductByExternalCode(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId,
            @PathParam("externalCode") String externalCode) {
        verifyServiceToken(serviceToken);
        ProductResponse product = getProductByExternalCodeUseCase.execute(requireStoreId(storeId), externalCode);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }

    @GET
    @Path("/products/{id}")
    public Response getProductById(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId,
            @PathParam("id") UUID id) {
        verifyServiceToken(serviceToken);
        return Response.ok(getProductUseCase.execute(requireStoreId(storeId), id)).build();
    }

    @POST
    @Path("/products")
    public Response createProduct(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId,
            @Valid ProductRequest request) {
        verifyServiceToken(serviceToken);
        ProductResponse created = createProductUseCase.execute(requireStoreId(storeId), request);
        return Response.created(URI.create("/api/v1/internal/catalog/products/" + created.id())).entity(created).build();
    }

    @PUT
    @Path("/products/{id}")
    public Response updateProduct(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId,
            @PathParam("id") UUID id,
            @Valid ProductRequest request) {
        verifyServiceToken(serviceToken);
        return Response.ok(updateProductUseCase.execute(requireStoreId(storeId), id, request)).build();
    }

    @GET
    @Path("/modifier-groups")
    public Response listModifierGroups(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {
        verifyServiceToken(serviceToken);
        return Response.ok(listModifierGroupsUseCase.execute(requireStoreId(storeId), page, size)).build();
    }

    @POST
    @Path("/modifier-groups")
    public Response createModifierGroup(
            @HeaderParam("X-Internal-Service-Token") String serviceToken,
            @HeaderParam("X-Store-Id") UUID storeId,
            @Valid ModifierGroupRequest request) {
        verifyServiceToken(serviceToken);
        ModifierGroupResponse created = createModifierGroupUseCase.execute(requireStoreId(storeId), request);
        return Response.created(URI.create("/api/v1/internal/catalog/modifier-groups/" + created.id())).entity(created).build();
    }

    private void verifyServiceToken(String provided) {
        if (provided == null || provided.isBlank() || expectedServiceToken == null || expectedServiceToken.isBlank()) {
            throw new InvalidInternalServiceTokenException("Token interno de comunicação entre microsserviços ausente ou inválido.");
        }
        boolean matches = MessageDigest.isEqual(
                expectedServiceToken.trim().getBytes(StandardCharsets.UTF_8),
                provided.trim().getBytes(StandardCharsets.UTF_8)
        );
        if (!matches) {
            throw new InvalidInternalServiceTokenException("Token interno de comunicação entre microsserviços inválido.");
        }
    }

    private UUID requireStoreId(UUID storeId) {
        if (storeId == null) {
            throw new InvalidSyncRequestException("Header X-Store-Id é obrigatório na integração interna de catálogo.");
        }
        return storeId;
    }
}
