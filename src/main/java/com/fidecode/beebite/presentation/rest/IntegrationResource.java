package com.fidecode.beebite.presentation.rest;

import com.fidecode.beebite.application.integration.*;
import com.fidecode.beebite.application.integration.dto.*;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/api/v1/integrations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE" })
public class IntegrationResource extends BaseResource {
    @Inject
    GetIntegrationsUseCase getUseCase;
    @Inject
    ConfigureWhatsappUseCase whatsappUseCase;
    @Inject
    ConfigureIfoodUseCase ifoodUseCase;
    @Inject
    AuthorizeIfoodUseCase authorizeUseCase;
    @Inject
    UpdateIfoodStoreStatusUseCase storeStatusUseCase;

    @GET
    @PermissionsAllowed("integracoes")
    public Response get() {
        return Response.ok(getUseCase.execute(getStoreId())).build();
    }

    @PUT
    @Path("/whatsapp")
    @PermissionsAllowed("integracoes")
    public Response whatsapp(WhatsappIntegrationRequest r) {
        return Response.ok(whatsappUseCase.execute(getStoreId(), r)).build();
    }

    @PUT
    @Path("/ifood")
    @PermissionsAllowed("integracoes")
    public Response ifood(IfoodIntegrationRequest r) {
        return Response.ok(ifoodUseCase.execute(getStoreId(), r)).build();
    }

    @POST
    @Path("/ifood/authorize")
    @PermissionsAllowed("integracoes")
    public Response authorize() {
        return Response.ok(authorizeUseCase.execute(getStoreId())).build();
    }

    @PATCH
    @Path("/ifood/store-status")
    @PermissionsAllowed("integracoes")
    public Response status(StoreStatusRequest r) {
        return Response.ok(storeStatusUseCase.execute(getStoreId(), r.open())).build();
    }

    public record StoreStatusRequest(boolean open) {
    }
}
