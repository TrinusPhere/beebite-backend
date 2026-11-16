package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.dashboard.GetDashboardUseCase;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;

/**
 * REST Resource — Dashboard (/api/v1/dashboard). Painel resumo com KPIs consolidados.
 */
@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE", "CAIXA" })
public class DashboardResource extends BaseResource {

    @Inject
    GetDashboardUseCase getDashboardUseCase;

    @PermissionsAllowed("dashboard")
    @GET
    public Response getDashboard(
            @QueryParam("period") @DefaultValue("daily") String period,
            @QueryParam("startDate") String startDateStr,
            @QueryParam("endDate") String endDateStr) {

        LocalDate startDate = (startDateStr != null && !startDateStr.isBlank())
                ? LocalDate.parse(startDateStr)
                : null;
        LocalDate endDate = (endDateStr != null && !endDateStr.isBlank())
                ? LocalDate.parse(endDateStr)
                : null;

        return Response.ok(getDashboardUseCase.execute(getStoreId(), period, startDate, endDate)).build();
    }
}
