package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.platform.*;
import com.trinusphere.beebite.application.platform.dto.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/platform")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("SUPER_ADMIN")
public class PlatformBackofficeResource extends BaseResource {

    @Inject
    GetPlatformOverviewUseCase getPlatformOverviewUseCase;
    @Inject
    ListTenantAccountsUseCase listTenantAccountsUseCase;
    @Inject
    OnboardClientTenantUseCase onboardClientTenantUseCase;
    @Inject
    AddStoreToTenantUseCase addStoreToTenantUseCase;
    @Inject
    ChangeTenantStatusUseCase changeTenantStatusUseCase;
    @Inject
    ListSaasContractsUseCase listSaasContractsUseCase;
    @Inject
    ListSaasInvoicesUseCase listSaasInvoicesUseCase;
    @Inject
    GenerateMonthlyInvoicesUseCase generateMonthlyInvoicesUseCase;
    @Inject
    MarkSaasInvoicePaidUseCase markSaasInvoicePaidUseCase;

    @GET
    @Path("/overview")
    public Response getOverview() {
        PlatformOverviewResponse overview = getPlatformOverviewUseCase.execute();
        return Response.ok(overview).build();
    }

    @GET
    @Path("/tenants")
    public Response listTenants() {
        List<TenantAccountResponse> tenants = listTenantAccountsUseCase.execute();
        return Response.ok(tenants).build();
    }

    @POST
    @Path("/tenants/onboard")
    public Response onboardClient(@Valid OnboardClientRequest request) {
        OnboardClientResponse response = onboardClientTenantUseCase.execute(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/tenants/{tenantId}/stores")
    public Response addStoreToTenant(
            @PathParam("tenantId") UUID tenantId,
            @Valid CreateStoreForTenantRequest request) {
        OnboardClientResponse.ProvisionedStoreDto created = addStoreToTenantUseCase.execute(tenantId, request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PATCH
    @Path("/tenants/{tenantId}/status")
    public Response changeTenantStatus(
            @PathParam("tenantId") UUID tenantId,
            @Valid ChangeTenantStatusRequest request) {
        changeTenantStatusUseCase.execute(tenantId, request);
        return Response.noContent().build();
    }

    @GET
    @Path("/contracts")
    public Response listContracts() {
        List<SaasContractResponse> contracts = listSaasContractsUseCase.execute();
        return Response.ok(contracts).build();
    }

    @GET
    @Path("/invoices")
    public Response listInvoices(
            @QueryParam("status") String status,
            @QueryParam("tenantId") UUID tenantId) {
        List<SaasInvoiceResponse> invoices = listSaasInvoicesUseCase.execute(status, tenantId);
        return Response.ok(invoices).build();
    }

    @POST
    @Path("/invoices/generate-month/{referenceMonth}")
    public Response generateMonthlyInvoices(@PathParam("referenceMonth") String referenceMonth) {
        List<SaasInvoiceResponse> generated = generateMonthlyInvoicesUseCase.execute(referenceMonth);
        return Response.ok(generated).build();
    }

    @POST
    @Path("/invoices/{invoiceId}/pay")
    public Response markInvoicePaid(
            @PathParam("invoiceId") UUID invoiceId,
            @Valid MarkInvoicePaidRequest request) {
        SaasInvoiceResponse paid = markSaasInvoicePaidUseCase.execute(invoiceId, request);
        return Response.ok(paid).build();
    }
}

