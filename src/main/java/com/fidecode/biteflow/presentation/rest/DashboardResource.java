package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.dashboard.GetDashboardUseCase;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Resource — Dashboard (/api/v1/dashboard).
 * Painel resumo com KPIs consolidados.
 */
@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE", "CAIXA"})
public class DashboardResource extends BaseResource {

    @Inject GetDashboardUseCase getDashboardUseCase;

    @PermissionsAllowed("dashboard")
    @GET
    public Response getDashboard() {
        return Response.ok(getDashboardUseCase.execute(getStoreId())).build();
    }
}
