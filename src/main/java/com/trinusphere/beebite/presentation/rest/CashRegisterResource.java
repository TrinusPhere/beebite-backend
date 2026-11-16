package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.application.cash.*;
import com.trinusphere.beebite.application.cash.dto.CashRegisterRequest;
import com.trinusphere.beebite.application.cash.dto.CashRegisterResponse;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/cash/registers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "GERENTE"})
public class CashRegisterResource extends BaseResource {

    private final CreateCashRegisterUseCase createUseCase;
    private final UpdateCashRegisterUseCase updateUseCase;
    private final DeleteCashRegisterUseCase deleteUseCase;
    private final ListCashRegistersUseCase listUseCase;
    private final GetCashRegisterUseCase getUseCase;

    @Inject
    public CashRegisterResource(CreateCashRegisterUseCase createUseCase,
                                UpdateCashRegisterUseCase updateUseCase,
                                DeleteCashRegisterUseCase deleteUseCase,
                                ListCashRegistersUseCase listUseCase,
                                GetCashRegisterUseCase getUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.listUseCase = listUseCase;
        this.getUseCase = getUseCase;
    }

    @GET
    @PermissionsAllowed("caixa")
    public List<CashRegisterResponse> list() {
        return listUseCase.execute(getStoreId());
    }

    @GET
    @Path("/{id}")
    @PermissionsAllowed("caixa")
    public CashRegisterResponse getById(@PathParam("id") UUID id) {
        return getUseCase.execute(getStoreId(), id);
    }

    @POST
    @PermissionsAllowed("caixa")
    public Response create(@Valid CashRegisterRequest request) {
        CashRegisterResponse response = createUseCase.execute(getStoreId(), request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @PermissionsAllowed("caixa")
    public CashRegisterResponse update(@PathParam("id") UUID id, @Valid CashRegisterRequest request) {
        return updateUseCase.execute(getStoreId(), id, request);
    }

    @DELETE
    @Path("/{id}")
    @PermissionsAllowed("caixa")
    public Response delete(@PathParam("id") UUID id) {
        deleteUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }
}
