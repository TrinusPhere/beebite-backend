package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.store.GetStoreInfoUseCase;
import com.fidecode.biteflow.application.store.UpdateStoreInfoUseCase;
import com.fidecode.biteflow.application.store.dto.StoreInfoRequest;
import com.fidecode.biteflow.application.store.dto.StoreInfoResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@Path("/api/v1/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class StoreResource {

    @Inject GetStoreInfoUseCase getStoreInfoUseCase;
    @Inject UpdateStoreInfoUseCase updateStoreInfoUseCase;
    @Inject JsonWebToken jwt;

    private static final UUID DEFAULT_STORE_ID =
        UUID.fromString("00000000-0000-0000-0000-000000000001");

    /**
     * GET /api/v1/store — qualquer usuário autenticado
     */
    @GET
    public Response getStore() {
        UUID storeId = resolveStoreId();
        StoreInfoResponse response = getStoreInfoUseCase.execute(storeId);
        return Response.ok(response).build();
    }

    /**
     * PUT /api/v1/store — apenas ADMIN
     */
    @PUT
    public Response updateStore(@Valid StoreInfoRequest request) {
        if (!jwt.getGroups().contains("ADMIN")) {
            throw new ForbiddenException("Apenas administradores podem alterar dados da loja.");
        }
        UUID storeId = resolveStoreId();
        StoreInfoResponse response = updateStoreInfoUseCase.execute(storeId, request);
        return Response.ok(response).build();
    }

    private UUID resolveStoreId() {
        String storeIdClaim = jwt.getClaim("storeId");
        return storeIdClaim != null ? UUID.fromString(storeIdClaim) : DEFAULT_STORE_ID;
    }
}
