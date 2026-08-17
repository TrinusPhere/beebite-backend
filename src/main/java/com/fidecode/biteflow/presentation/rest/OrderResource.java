package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.order.CreateOrderUseCase;
import com.fidecode.biteflow.application.order.UpdateOrderUseCase;
import com.fidecode.biteflow.application.order.GetOrderUseCase;
import com.fidecode.biteflow.application.order.ListOrdersUseCase;
import com.fidecode.biteflow.application.order.ManageOrderUseCase;
import com.fidecode.biteflow.application.order.GetOrderKanbanUseCase;
import com.fidecode.biteflow.application.order.DeleteOrderUseCase;
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

    @Inject CreateOrderUseCase createOrderUseCase;
    @Inject UpdateOrderUseCase updateOrderUseCase;
    @Inject GetOrderUseCase getOrderUseCase;
    @Inject ListOrdersUseCase listOrdersUseCase;
    @Inject ManageOrderUseCase manageOrderUseCase;
    @Inject GetOrderKanbanUseCase getOrderKanbanUseCase;
    @Inject DeleteOrderUseCase deleteOrderUseCase;
    @Inject JsonWebToken jwt;

    @GET
    public Response list(
            @QueryParam("status") String status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        checkPermission("pedidos");
        return Response.ok(listOrdersUseCase.list(getStoreId(), status, page, size)).build();
    }

    @GET
    @Path("/kanban")
    public Response listKanban(@QueryParam("type") String typeFilter) {
        checkPermission("pedidos");
        return Response.ok(getOrderKanbanUseCase.listKanban(getStoreId(), typeFilter)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        checkPermission("pedidos");
        return Response.ok(getOrderUseCase.getById(getStoreId(), id)).build();
    }

    @POST
    public Response create(@Valid CreateOrderRequest request) {
        checkPermission("pedidos");
        OrderResponse created = createOrderUseCase.create(getStoreId(), request);
        return Response.created(URI.create("/api/v1/orders/" + created.id()))
            .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid UpdateOrderRequest request) {
        checkPermission("pedidos");
        return Response.ok(updateOrderUseCase.update(getStoreId(), id, request)).build();
    }

    @PATCH
    @Path("/{id}/advance")
    public Response advanceStatus(@PathParam("id") UUID id) {
        checkPermission("pedidos");
        return Response.ok(manageOrderUseCase.advanceStatus(getStoreId(), id)).build();
    }

    @PATCH
    @Path("/{id}/move")
    public Response moveToColumn(@PathParam("id") UUID id, @Valid MoveOrderRequest request) {
        checkPermission("pedidos");
        return Response.ok(manageOrderUseCase.moveToColumn(getStoreId(), id, request.targetStatus())).build();
    }

    @PATCH
    @Path("/{id}/assign-driver")
    public Response assignDriver(@PathParam("id") UUID id, @Valid AssignDriverRequest request) {
        checkPermission("pedidos");
        return Response.ok(manageOrderUseCase.assignDriver(getStoreId(), id, request)).build();
    }

    @POST
    @Path("/{id}/nfe")
    public Response emitNfe(@PathParam("id") UUID id) {
        checkPermission("pedidos");
        return Response.ok(manageOrderUseCase.emitNfe(getStoreId(), id)).build();
    }

    @PATCH
    @Path("/batch/move")
    public Response batchMove(@Valid BatchMoveRequest request) {
        checkPermission("pedidos");
        manageOrderUseCase.batchMove(getStoreId(), request.orderIds(),
            new MoveOrderRequest(request.targetStatus()));
        return Response.noContent().build();
    }

    @PATCH
    @Path("/batch/assign-driver")
    public Response batchAssignDriver(@Valid AssignDriverRequest request) {
        checkPermission("pedidos");
        manageOrderUseCase.batchAssignDriver(getStoreId(), request);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkPermission("pedidos");
        deleteOrderUseCase.delete(getStoreId(), id);
        return Response.noContent().build();
    }

    // Internal record for batch move
    public record BatchMoveRequest(
        @jakarta.validation.constraints.NotNull(message = "Lista de pedidos é obrigatória")
        @jakarta.validation.constraints.Size(min = 1, message = "Selecione pelo menos um pedido")
        List<String> orderIds,
        
        @jakarta.validation.constraints.NotBlank(message = "Status alvo é obrigatório")
        String targetStatus
    ) {}

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
