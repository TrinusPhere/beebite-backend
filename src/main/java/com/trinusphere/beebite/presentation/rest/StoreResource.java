package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.store.GetStoreInfoUseCase;
import com.trinusphere.beebite.application.store.UpdateStoreInfoUseCase;
import com.trinusphere.beebite.application.store.dto.StoreInfoRequest;
import com.trinusphere.beebite.application.store.dto.StoreInfoResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@RolesAllowed("ADMIN")
public class StoreResource extends BaseResource {

    @Inject
    GetStoreInfoUseCase getStoreInfoUseCase;
    @Inject
    UpdateStoreInfoUseCase updateStoreInfoUseCase;

    /**
     * GET /api/v1/store — qualquer usuário autenticado
     */
    @GET
    public Response getStore() {
        StoreInfoResponse response = getStoreInfoUseCase.execute(getStoreId());
        return Response.ok(response).build();
    }

    /**
     * PUT /api/v1/store — apenas ADMIN
     */
    @PUT
    public Response updateStore(@Valid StoreInfoRequest request) {
        StoreInfoResponse response = updateStoreInfoUseCase.execute(getStoreId(), request);
        return Response.ok(response).build();
    }
}
