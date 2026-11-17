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
    UpdateTenantAccountUseCase updateTenantAccountUseCase;
    @Inject
    AddStoreToTenantUseCase addStoreToTenantUseCase;
    @Inject
    UpdatePlatformStoreUseCase updatePlatformStoreUseCase;
    @Inject
    RegenerateEdgeTokenUseCase regenerateEdgeTokenUseCase;
    @Inject
    ChangeTenantStatusUseCase changeTenantStatusUseCase;
    @Inject
    ListSaasContractsUseCase listSaasContractsUseCase;
    @Inject
    UpdateSaasContractUseCase updateSaasContractUseCase;
    @Inject
    ListSaasInvoicesUseCase listSaasInvoicesUseCase;
    @Inject
    GenerateMonthlyInvoicesUseCase generateMonthlyInvoicesUseCase;
    @Inject
    CreateSaasInvoiceUseCase createSaasInvoiceUseCase;
    @Inject
    MarkSaasInvoicePaidUseCase markSaasInvoicePaidUseCase;
    @Inject
    CancelSaasInvoiceUseCase cancelSaasInvoiceUseCase;

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

    @PUT
    @Path("/tenants/{tenantId}")
    public Response updateTenant(
            @PathParam("tenantId") UUID tenantId,
            @Valid UpdateTenantAccountRequest request) {
        updateTenantAccountUseCase.execute(tenantId, request);
        return Response.noContent().build();
    }

    @POST
    @Path("/tenants/{tenantId}/stores")
    public Response addStoreToTenant(
            @PathParam("tenantId") UUID tenantId,
            @Valid CreateStoreForTenantRequest request) {
        OnboardClientResponse.ProvisionedStoreDto created = addStoreToTenantUseCase.execute(tenantId, request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/stores/{storeId}")
    public Response updateStore(
            @PathParam("storeId") UUID storeId,
            @Valid UpdatePlatformStoreRequest request) {
        TenantAccountResponse.StoreSummaryDto updated = updatePlatformStoreUseCase.execute(storeId, request);
        return Response.ok(updated).build();
    }

    @POST
    @Path("/stores/{storeId}/regenerate-edge-token")
    public Response regenerateEdgeToken(@PathParam("storeId") UUID storeId) {
        TenantAccountResponse.StoreSummaryDto updated = regenerateEdgeTokenUseCase.execute(storeId);
        return Response.ok(updated).build();
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

    @PUT
    @Path("/contracts/{contractId}")
    public Response updateContract(
            @PathParam("contractId") UUID contractId,
            @Valid UpdateSaasContractRequest request) {
        SaasContractResponse updated = updateSaasContractUseCase.execute(contractId, request);
        return Response.ok(updated).build();
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
    @Path("/invoices")
    public Response createInvoice(@Valid CreateSaasInvoiceRequest request) {
        SaasInvoiceResponse created = createSaasInvoiceUseCase.execute(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
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

    @PATCH
    @Path("/invoices/{invoiceId}/cancel")
    public Response cancelInvoice(
            @PathParam("invoiceId") UUID invoiceId,
            CancelSaasInvoiceRequest request) {
        String notes = request != null ? request.notes() : null;
        SaasInvoiceResponse cancelled = cancelSaasInvoiceUseCase.execute(invoiceId, notes);
        return Response.ok(cancelled).build();
    }
}
