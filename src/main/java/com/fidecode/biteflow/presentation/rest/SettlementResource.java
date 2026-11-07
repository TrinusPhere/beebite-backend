package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.delivery.GetSettlementReportUseCase;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/settlements")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE" })
public class SettlementResource extends BaseResource {

    @Inject
    GetSettlementReportUseCase getSettlementReportUseCase;

    @PermissionsAllowed({ "acerto_entregadores", "entregadores", "financeiro" })
    @GET
    public Response getReport(
            @QueryParam("period") @DefaultValue("daily") String period,
            @QueryParam("date") String date) {
        return Response.ok(getSettlementReportUseCase.execute(getStoreId(), period, date)).build();
    }
}
