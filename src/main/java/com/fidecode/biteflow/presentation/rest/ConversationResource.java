package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.messaging.*;
import com.fidecode.biteflow.application.messaging.dto.*;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.UUID;

@Path("/api/v1/conversations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE", "CAIXA" })
public class ConversationResource extends BaseResource {
    @Inject
    ListConversationsUseCase listUseCase;
    @Inject
    ResolveConversationUseCase resolveUseCase;
    @Inject
    SendConversationMessageUseCase sendUseCase;
    @Inject
    UpdateConversationUseCase updateUseCase;

    @GET
    @PermissionsAllowed("pedidos")
    public Response list() {
        return Response.ok(listUseCase.execute(getStoreId())).build();
    }

    @POST
    @Path("/resolve")
    @PermissionsAllowed("pedidos")
    public Response resolve(ResolveConversationRequest r) {
        return Response.ok(resolveUseCase.execute(getStoreId(), r)).build();
    }

    @POST
    @Path("/{id}/messages")
    @PermissionsAllowed("pedidos")
    public Response send(@PathParam("id") UUID id, @Valid SendMessageRequest r) {
        return Response.status(Response.Status.CREATED).entity(sendUseCase.execute(getStoreId(), id, r)).build();
    }

    @PATCH
    @Path("/{id}")
    @PermissionsAllowed("pedidos")
    public Response update(@PathParam("id") UUID id, ConversationPatchRequest r) {
        return Response.ok(updateUseCase.execute(getStoreId(), id, r)).build();
    }
}
