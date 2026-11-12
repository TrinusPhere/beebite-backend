package com.fidecode.beebite.presentation.rest;

import com.fidecode.beebite.application.printing.*;
import com.fidecode.beebite.application.printing.dto.PrinterRequest;
import io.quarkus.security.PermissionsAllowed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.UUID;

@Path("/api/v1/printers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMIN", "GERENTE" })
public class PrinterResource extends BaseResource {
    @Inject
    ListPrintersUseCase listUseCase;
    @Inject
    CreatePrinterUseCase createUseCase;
    @Inject
    DeletePrinterUseCase deleteUseCase;
    @Inject
    TestPrinterUseCase testUseCase;

    @GET
    @PermissionsAllowed("produtos")
    public Response list() {
        return Response.ok(listUseCase.execute(getStoreId())).build();
    }

    @POST
    @PermissionsAllowed("produtos")
    public Response save(@Valid PrinterRequest r) {
        return Response.status(Response.Status.CREATED).entity(createUseCase.execute(getStoreId(), r)).build();
    }

    @DELETE
    @Path("/{id}")
    @PermissionsAllowed("produtos")
    public Response delete(@PathParam("id") UUID id) {
        deleteUseCase.execute(getStoreId(), id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/test")
    @PermissionsAllowed("produtos")
    public Response test(@PathParam("id") UUID id) {
        return Response.ok(testUseCase.execute(getStoreId(), id)).build();
    }
}
