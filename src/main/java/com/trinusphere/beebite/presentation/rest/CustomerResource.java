package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.customer.*;
import com.trinusphere.beebite.application.customer.dto.CustomerRequest;
import com.trinusphere.beebite.application.customer.dto.CustomerResponse;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@RolesAllowed({ "ADMIN", "GERENTE", "CAIXA" })
public class CustomerResource extends BaseResource {

    @Inject
    CreateCustomerUseCase createCustomerUseCase;
    @Inject
    UpdateCustomerUseCase updateCustomerUseCase;
    @Inject
    DeleteCustomerUseCase deleteCustomerUseCase;
    @Inject
    GetCustomerUseCase getCustomerUseCase;
    @Inject
    ListCustomersUseCase listCustomersUseCase;

    @PermissionsAllowed("clientes")
    @GET
    public Response list(@QueryParam("search") String search, @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return Response.ok(listCustomersUseCase.execute(getStoreId(), search, page, size)).build();
    }

    @PermissionsAllowed("clientes")
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return Response.ok(getCustomerUseCase.execute(getStoreId(), id)).build();
    }

    @PermissionsAllowed("clientes")
    @POST
    public Response create(@Valid CustomerRequest request) {
        CustomerResponse created = createCustomerUseCase.execute(getStoreId(), request);
        return Response.created(URI.create("/api/v1/customers/" + created.id())).entity(created).build();
    }

    @PermissionsAllowed("clientes")
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid CustomerRequest request) {
        return Response.ok(updateCustomerUseCase.execute(getStoreId(), id, request)).build();
    }

    @PermissionsAllowed("clientes")
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteCustomerUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }
}
