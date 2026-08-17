package com.fidecode.biteflow.presentation.rest;

import com.fidecode.biteflow.application.customer.CreateCustomerUseCase;
import com.fidecode.biteflow.application.customer.DeleteCustomerUseCase;
import com.fidecode.biteflow.application.customer.GetCustomerUseCase;
import com.fidecode.biteflow.application.customer.ListCustomersUseCase;
import com.fidecode.biteflow.application.customer.UpdateCustomerUseCase;
import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
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

@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class CustomerResource {

    @Inject CreateCustomerUseCase createCustomerUseCase;
    @Inject UpdateCustomerUseCase updateCustomerUseCase;
    @Inject DeleteCustomerUseCase deleteCustomerUseCase;
    @Inject GetCustomerUseCase getCustomerUseCase;
    @Inject ListCustomersUseCase listCustomersUseCase;
    @Inject JsonWebToken jwt;

    @GET
    public Response list(
            @QueryParam("search") String search,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        checkPermission("clientes");
        return Response.ok(listCustomersUseCase.execute(getStoreId(), search, page, size)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        checkPermission("clientes");
        return Response.ok(getCustomerUseCase.execute(getStoreId(), id)).build();
    }

    @POST
    public Response create(@Valid CustomerRequest request) {
        checkPermission("clientes");
        CustomerResponse created = createCustomerUseCase.execute(getStoreId(), request);
        return Response.created(URI.create("/api/v1/customers/" + created.id()))
            .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid CustomerRequest request) {
        checkPermission("clientes");
        return Response.ok(updateCustomerUseCase.execute(getStoreId(), id, request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkPermission("clientes");
        deleteCustomerUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }

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
