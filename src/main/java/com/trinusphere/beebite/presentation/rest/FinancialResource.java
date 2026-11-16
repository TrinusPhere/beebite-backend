package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.financial.*;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Resource — Financeiro (/api/v1/financial). Relatórios de faturamento, vendas por produto e formas de pagamento.
 */
@Path("/api/v1/financial")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE" })
public class FinancialResource extends BaseResource {

    @Inject
    GetSalesSummaryUseCase getSalesSummaryUseCase;
    @Inject
    GetSalesByProductUseCase getSalesByProductUseCase;
    @Inject
    GetPaymentMethodsReportUseCase getPaymentMethodsReportUseCase;
    @Inject
    ListFinancialRecordsUseCase listFinancialRecordsUseCase;

    @PermissionsAllowed("financeiro")
    @GET
    @Path("/summary")
    public Response getSummary(@QueryParam("startDate") long startDate, @QueryParam("endDate") long endDate) {
        return Response.ok(getSalesSummaryUseCase.execute(getStoreId(), startDate, endDate)).build();
    }

    @PermissionsAllowed("financeiro")
    @GET
    @Path("/sales-by-product")
    public Response getSalesByProduct(@QueryParam("startDate") long startDate, @QueryParam("endDate") long endDate) {
        return Response.ok(getSalesByProductUseCase.execute(getStoreId(), startDate, endDate)).build();
    }

    @PermissionsAllowed("financeiro")
    @GET
    @Path("/payment-methods")
    public Response getPaymentMethods(@QueryParam("startDate") long startDate, @QueryParam("endDate") long endDate) {
        return Response.ok(getPaymentMethodsReportUseCase.execute(getStoreId(), startDate, endDate)).build();
    }

    @PermissionsAllowed("financeiro")
    @GET
    @Path("/records")
    public Response listRecords(@QueryParam("startDate") long startDate, @QueryParam("endDate") long endDate) {
        return Response.ok(listFinancialRecordsUseCase.execute(getStoreId(), startDate, endDate)).build();
    }
}
