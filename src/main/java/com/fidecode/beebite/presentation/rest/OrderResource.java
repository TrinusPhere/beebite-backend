package com.fidecode.beebite.presentation.rest;

import com.fidecode.beebite.application.order.*;
import com.fidecode.beebite.application.order.dto.*;
import com.fidecode.beebite.application.printing.PrintOrderUseCase;
import com.fidecode.beebite.application.printing.dto.PrintOrderRequest;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@RolesAllowed({ "ADMIN", "GERENTE", "CAIXA" })
public class OrderResource extends BaseResource {

    @Inject
    CreateOrderUseCase createOrderUseCase;
    @Inject
    UpdateOrderUseCase updateOrderUseCase;
    @Inject
    GetOrderUseCase getOrderUseCase;
    @Inject
    ListOrdersUseCase listOrdersUseCase;
    @Inject
    AdvanceOrderStatusUseCase advanceOrderStatusUseCase;
    @Inject
    MoveOrderToColumnUseCase moveOrderToColumnUseCase;
    @Inject
    AssignOrderDriverUseCase assignOrderDriverUseCase;
    @Inject
    BatchMoveOrdersUseCase batchMoveOrdersUseCase;
    @Inject
    BatchAssignOrderDriverUseCase batchAssignOrderDriverUseCase;
    @Inject
    EmitOrderNfeUseCase emitOrderNfeUseCase;
    @Inject
    GetOrderKanbanUseCase getOrderKanbanUseCase;
    @Inject
    DeleteOrderUseCase deleteOrderUseCase;
    @Inject
    PrintOrderUseCase printOrderUseCase;
    @Inject
    GetOrderHistoryUseCase getOrderHistoryUseCase;

    @PermissionsAllowed("pedidos")
    @GET
    @Path("/history")
    public Response getHistory(@QueryParam("date") String dateStr) {
        java.time.LocalDate date = (dateStr != null && !dateStr.isBlank())
                ? java.time.LocalDate.parse(dateStr)
                : java.time.LocalDate.now();
        return Response.ok(getOrderHistoryUseCase.execute(getStoreId(), date)).build();
    }

    @PermissionsAllowed("pedidos")
    @GET
    public Response list(@QueryParam("status") String status, @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(listOrdersUseCase.list(getStoreId(), status, page, size)).build();
    }

    @PermissionsAllowed("pedidos")
    @GET
    @Path("/kanban")
    public Response listKanban(@QueryParam("type") String typeFilter) {
        return Response.ok(getOrderKanbanUseCase.listKanban(getStoreId(), typeFilter)).build();
    }

    @PermissionsAllowed("pedidos")
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return Response.ok(getOrderUseCase.getById(getStoreId(), id)).build();
    }

    @PermissionsAllowed("pedidos")
    @POST
    public Response create(@Valid CreateOrderRequest request) {
        OrderResponse created = createOrderUseCase.create(getStoreId(), request);
        return Response.created(URI.create("/api/v1/orders/" + created.id())).entity(created).build();
    }

    @PermissionsAllowed("pedidos")
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid UpdateOrderRequest request) {
        return Response.ok(updateOrderUseCase.update(getStoreId(), id, request)).build();
    }

    @PermissionsAllowed("pedidos")
    @PATCH
    @Path("/{id}/advance")
    public Response advanceStatus(@PathParam("id") UUID id) {
        return Response.ok(advanceOrderStatusUseCase.execute(getStoreId(), id)).build();
    }

    @PermissionsAllowed("pedidos")
    @PATCH
    @Path("/{id}/move")
    public Response moveToColumn(@PathParam("id") UUID id, @Valid MoveOrderRequest request) {
        return Response.ok(moveOrderToColumnUseCase.execute(getStoreId(), id, request.targetStatus())).build();
    }

    @PermissionsAllowed("pedidos")
    @PATCH
    @Path("/{id}/assign-driver")
    public Response assignDriver(@PathParam("id") UUID id, @Valid AssignDriverRequest request) {
        return Response.ok(assignOrderDriverUseCase.execute(getStoreId(), id, request)).build();
    }

    @PermissionsAllowed("pedidos")
    @POST
    @Path("/{id}/nfe")
    public Response emitNfe(@PathParam("id") UUID id) {
        return Response.ok(emitOrderNfeUseCase.execute(getStoreId(), id)).build();
    }

    @PermissionsAllowed("pedidos")
    @PATCH
    @Path("/batch/move")
    public Response batchMove(@Valid BatchMoveRequest request) {
        batchMoveOrdersUseCase.execute(getStoreId(), request.orderIds(), new MoveOrderRequest(request.targetStatus()));
        return Response.noContent().build();
    }

    @PermissionsAllowed("pedidos")
    @PATCH
    @Path("/batch/assign-driver")
    public Response batchAssignDriver(@Valid AssignDriverRequest request) {
        batchAssignOrderDriverUseCase.execute(getStoreId(), request);
        return Response.noContent().build();
    }

    @PermissionsAllowed("pedidos")
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteOrderUseCase.delete(getStoreId(), id);
        return Response.noContent().build();
    }

    @PermissionsAllowed("pedidos")
    @POST
    @Path("/{id}/print")
    public Response print(@PathParam("id") UUID id, @Valid PrintOrderRequest request) {
        return Response.ok(printOrderUseCase.execute(getStoreId(), id, request)).build();
    }

    // Internal record for batch move
    public record BatchMoveRequest(
            @jakarta.validation.constraints.NotNull(message = "Lista de pedidos é obrigatória") @jakarta.validation.constraints.Size(min = 1, message = "Selecione pelo menos um pedido") List<String> orderIds,

            @jakarta.validation.constraints.NotBlank(message = "Status alvo é obrigatório") String targetStatus) {
    }
}
