package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.order.OrderUseCases;
import com.fidecode.biteflow.application.order.dto.*;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class OrderResource {

    @Inject OrderUseCases orderUseCases;
    @Inject JsonWebToken jwt;

    @GET
    @Path("/kanban")
    public Response listKanban(@QueryParam("type") String typeFilter) {
        checkPermission("pedidos");
        return Response.ok(orderUseCases.listKanban(getStoreId(), typeFilter)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        checkPermission("pedidos");
        return Response.ok(orderUseCases.getById(id)).build();
    }

    @POST
    public Response create(@Valid CreateOrderRequest request) {
        checkPermission("pedidos");
        OrderResponse created = orderUseCases.create(getStoreId(), request);
        return Response.created(URI.create("/api/v1/orders/" + created.id()))
            .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, UpdateOrderRequest request) {
        checkPermission("pedidos");
        return Response.ok(orderUseCases.update(id, request)).build();
    }

    @PATCH
    @Path("/{id}/advance")
    public Response advanceStatus(@PathParam("id") UUID id) {
        checkPermission("pedidos");
        return Response.ok(orderUseCases.advanceStatus(id)).build();
    }

    @PATCH
    @Path("/{id}/move")
    public Response moveToColumn(@PathParam("id") UUID id, @Valid MoveOrderRequest request) {
        checkPermission("pedidos");
        return Response.ok(orderUseCases.moveToColumn(id, request)).build();
    }

    @PATCH
    @Path("/{id}/assign-driver")
    public Response assignDriver(@PathParam("id") UUID id, @Valid AssignDriverRequest request) {
        checkPermission("pedidos");
        return Response.ok(orderUseCases.assignDriver(id, request)).build();
    }

    @POST
    @Path("/{id}/nfe")
    public Response emitNfe(@PathParam("id") UUID id) {
        checkPermission("pedidos");
        return Response.ok(orderUseCases.emitNfe(id)).build();
    }

    @PATCH
    @Path("/batch/move")
    public Response batchMove(BatchMoveRequest request) {
        checkPermission("pedidos");
        orderUseCases.batchMove(request.orderIds(),
            new MoveOrderRequest(request.targetStatus()));
        return Response.noContent().build();
    }

    @PATCH
    @Path("/batch/assign-driver")
    public Response batchAssignDriver(@Valid AssignDriverRequest request) {
        checkPermission("pedidos");
        orderUseCases.batchAssignDriver(request);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkPermission("pedidos");
        orderUseCases.delete(id);
        return Response.noContent().build();
    }

    // Internal record for batch move
    public record BatchMoveRequest(List<String> orderIds, String targetStatus) {}

    private UUID getStoreId() {
        String storeIdClaim = jwt.getClaim("storeId");
        return storeIdClaim != null ? UUID.fromString(storeIdClaim)
            : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private void checkPermission(String required) {
        List<String> permissions = jwt.getClaim("permissions");
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        if (!isAdmin && (permissions == null || !permissions.contains(required))) {
            throw new ForbiddenException("Permissão '" + required + "' necessária.");
        }
    }
}
