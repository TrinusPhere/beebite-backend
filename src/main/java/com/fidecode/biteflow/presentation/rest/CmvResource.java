package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.financial.CalculateGeneralCmvUseCase;
import com.fidecode.biteflow.application.financial.CalculateProductCmvUseCase;
import com.fidecode.biteflow.application.financial.dto.GeneralCmvRequest;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * REST Resource — CMV (/api/v1/cmv). Cálculo de Custo da Mercadoria Vendida.
 */
@Path("/api/v1/cmv")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE" })
public class CmvResource extends BaseResource {

    @Inject
    CalculateGeneralCmvUseCase calculateGeneralCmvUseCase;
    @Inject
    CalculateProductCmvUseCase calculateProductCmvUseCase;

    @PermissionsAllowed("financeiro")
    @POST
    @Path("/general")
    public Response calculateGeneral(@Valid GeneralCmvRequest request) {
        return Response.ok(calculateGeneralCmvUseCase.execute(getStoreId(), request)).build();
    }

    @PermissionsAllowed("financeiro")
    @GET
    @Path("/product/{productId}")
    public Response calculateProduct(@PathParam("productId") UUID productId) {
        return Response.ok(calculateProductCmvUseCase.execute(getStoreId(), productId)).build();
    }
}
