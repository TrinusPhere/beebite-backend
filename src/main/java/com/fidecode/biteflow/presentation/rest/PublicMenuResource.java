package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.publicmenu.*;
import com.fidecode.biteflow.application.publicmenu.dto.PublicOrderRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/api/v1/public/stores/{identifier}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicMenuResource extends BaseResource {
    @Inject
    GetPublicMenuUseCase getMenuUseCase;
    @Inject
    CreatePublicOrderUseCase createOrderUseCase;

    @GET
    @Path("/menu")
    public Response menu(@PathParam("identifier") String id) {
        return Response.ok(getMenuUseCase.execute(id)).build();
    }

    @Inject
    com.fidecode.biteflow.domain.store.repository.StoreRepository stores;
    @Inject
    com.fidecode.biteflow.application.order.GetOrderUseCase getOrderUseCase;
    @Inject
    com.fidecode.biteflow.application.customer.GetCustomerByPhoneUseCase getCustomerByPhoneUseCase;
    @Inject
    com.fidecode.biteflow.application.customer.CreateCustomerUseCase createCustomerUseCase;

    @POST
    @Path("/orders")
    public Response create(@PathParam("identifier") String id, @Valid PublicOrderRequest r) {
        return Response.status(Response.Status.CREATED).entity(createOrderUseCase.execute(id, r)).build();
    }

    @GET
    @Path("/orders/{orderId}")
    public Response getOrder(@PathParam("identifier") String identifier, @PathParam("orderId") java.util.UUID orderId) {
        var store = stores.findByPublicIdentifier(identifier).orElseThrow(() -> new com.fidecode.biteflow.domain.store.exception.StoreNotFoundException(identifier));
        return Response.ok(getOrderUseCase.getById(store.getId(), orderId)).build();
    }

    @GET
    @Path("/customers/{phone}")
    public Response getCustomerByPhone(@PathParam("identifier") String identifier, @PathParam("phone") String phone) {
        var store = stores.findByPublicIdentifier(identifier).orElseThrow(() -> new com.fidecode.biteflow.domain.store.exception.StoreNotFoundException(identifier));
        return Response.ok(getCustomerByPhoneUseCase.execute(store.getId(), phone)).build();
    }

    @POST
    @Path("/customers")
    public Response createCustomer(@PathParam("identifier") String identifier, @Valid com.fidecode.biteflow.application.customer.dto.CustomerRequest request) {
        var store = stores.findByPublicIdentifier(identifier).orElseThrow(() -> new com.fidecode.biteflow.domain.store.exception.StoreNotFoundException(identifier));
        return Response.status(Response.Status.CREATED).entity(createCustomerUseCase.execute(store.getId(), request)).build();
    }
}
